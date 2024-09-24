/*
 * Copyright (c) 2024, Alibaba Cloud;
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aliyun.dataworks.migrationx.transformer.core.sqoop;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

/**
 * Tool that performs database imports to HDFS.
 */
public class ImportTool extends BaseSqoopTool {

    public static final Logger LOG = LoggerFactory.getLogger(ImportTool.class.getName());

    protected String[] extraArguments;

    // true if this is an all-tables import. Set by a subclass which
    // overrides the run() method of this tool (which can only do
    // a single table).
    private boolean allTables;

    // store check column type for incremental option
    private int checkColumnType;

    // Set classloader for local job runner
    private ClassLoader prevClassLoader = null;

    public ImportTool() {
        this("import", false);
    }

    public ImportTool(String toolName, boolean allTables) {
        super(toolName);
        this.allTables = allTables;
    }

    /**
     * @return true if the supplied options specify an incremental import.
     */
    private boolean isIncremental(SqoopOptions options) {
        return !options.getIncrementalMode().equals(SqoopOptions.IncrementalMode.None);
    }

    /**
     * Determine if a column is date/time.
     *
     * @return true if column type is TIMESTAMP, DATE, or TIME.
     */
    private boolean isDateTimeColumn(int columnType) {
        return (columnType == Types.TIMESTAMP) || (columnType == Types.DATE) || (columnType == Types.TIME);
    }

    /**
     * Construct the set of options that control imports, either of one table or a
     * batch of tables.
     *
     * @return the RelatedOptions that can be used to parse the import arguments.
     */
    @SuppressWarnings("static-access")
    protected RelatedOptions getImportOptions() {
        // Imports
        RelatedOptions importOpts = new RelatedOptions("Import control arguments");

        importOpts.addOption(
            OptionBuilder.withDescription("Use direct import fast path").withLongOpt(DIRECT_ARG).create());

        if (!allTables) {
            importOpts.addOption(OptionBuilder.withArgName("table-name").hasArg().withDescription("Table to read")
                .withLongOpt(TABLE_ARG).create());
            importOpts.addOption(OptionBuilder.withArgName("col,col,col...").hasArg()
                .withDescription("Columns to import from table").withLongOpt(COLUMNS_ARG).create());
            importOpts.addOption(OptionBuilder.withArgName("column-name").hasArg()
                .withDescription("Column of the table used to split work units").withLongOpt(SPLIT_BY_ARG)
                .create());
            importOpts.addOption(OptionBuilder.withArgName("size").hasArg().withDescription(
                    "Upper Limit of rows per split for split columns of Date/Time/Timestamp and integer types. For "
                        + "date or timestamp fields it is "
                        + "calculated in seconds. split-limit should be greater than 0")
                .withLongOpt(SPLIT_LIMIT_ARG).create());
            importOpts.addOption(OptionBuilder.withArgName("where clause").hasArg()
                .withDescription("WHERE clause to use during import").withLongOpt(WHERE_ARG).create());
            importOpts.addOption(
                OptionBuilder.withDescription("Imports data in append mode").withLongOpt(APPEND_ARG).create());
            importOpts.addOption(
                OptionBuilder.withDescription("Imports data in delete mode").withLongOpt(DELETE_ARG).create());
            importOpts.addOption(OptionBuilder.withArgName("dir").hasArg()
                .withDescription("HDFS plain table destination").withLongOpt(TARGET_DIR_ARG).create());
            importOpts.addOption(
                OptionBuilder.withArgName("statement").hasArg().withDescription("Import results of SQL 'statement'")
                    .withLongOpt(SQL_QUERY_ARG).create(SQL_QUERY_SHORT_ARG));
            importOpts.addOption(OptionBuilder.withArgName("statement").hasArg()
                .withDescription("Set boundary query for retrieving max and min" + " value of the primary key")
                .withLongOpt(SQL_QUERY_BOUNDARY).create());
            importOpts.addOption(OptionBuilder.withArgName("column").hasArg()
                .withDescription("Key column to use to join results").withLongOpt(MERGE_KEY_ARG).create());

            addValidationOpts(importOpts);
        }

        importOpts.addOption(OptionBuilder.withArgName("dir").hasArg()
            .withDescription("HDFS parent for table destination").withLongOpt(WAREHOUSE_DIR_ARG).create());
        importOpts.addOption(OptionBuilder.withDescription("Imports data to SequenceFiles")
            .withLongOpt(FMT_SEQUENCEFILE_ARG).create());
        importOpts.addOption(OptionBuilder.withDescription("Imports data as plain text (default)")
            .withLongOpt(FMT_TEXTFILE_ARG).create());
        importOpts.addOption(OptionBuilder.withDescription("Imports data to Avro data files")
            .withLongOpt(FMT_AVRODATAFILE_ARG).create());
        importOpts.addOption(OptionBuilder.withDescription("Imports data to Parquet files")
            .withLongOpt(FMT_PARQUETFILE_ARG).create());
        importOpts.addOption(
            OptionBuilder.withArgName("n").hasArg().withDescription("Use 'n' map tasks to import in parallel")
                .withLongOpt(NUM_MAPPERS_ARG).create(NUM_MAPPERS_SHORT_ARG));
        importOpts.addOption(OptionBuilder.withArgName("name").hasArg()
            .withDescription("Set name for generated mapreduce job").withLongOpt(MAPREDUCE_JOB_NAME).create());
        importOpts.addOption(OptionBuilder.withDescription("Enable compression").withLongOpt(COMPRESS_ARG)
            .create(COMPRESS_SHORT_ARG));
        importOpts.addOption(OptionBuilder.withArgName("codec").hasArg()
            .withDescription("Compression codec to use for import").withLongOpt(COMPRESSION_CODEC_ARG).create());
        importOpts.addOption(OptionBuilder.withArgName("n").hasArg()
            .withDescription("Split the input stream every 'n' bytes " + "when importing in direct mode")
            .withLongOpt(DIRECT_SPLIT_SIZE_ARG).create());
        importOpts.addOption(OptionBuilder.withArgName("n").hasArg()
            .withDescription("Set the maximum size for an inline LOB").withLongOpt(INLINE_LOB_LIMIT_ARG).create());
        importOpts.addOption(OptionBuilder.withArgName("n").hasArg()
            .withDescription("Set number 'n' of rows to fetch from the " + "database when more rows are needed")
            .withLongOpt(FETCH_SIZE_ARG).create());
        importOpts.addOption(OptionBuilder.withArgName("reset-mappers")
            .withDescription("Reset the number of mappers to one mapper if no split key available")
            .withLongOpt(AUTORESET_TO_ONE_MAPPER).create());
        return importOpts;
    }

    /**
     * Return options for incremental import.
     */
    protected RelatedOptions getIncrementalOptions() {
        RelatedOptions incrementalOpts = new RelatedOptions("Incremental import arguments");

        incrementalOpts.addOption(OptionBuilder.withArgName("import-type").hasArg()
            .withDescription("Define an incremental import of type 'append' or 'lastmodified'")
            .withLongOpt(INCREMENT_TYPE_ARG).create());
        incrementalOpts.addOption(OptionBuilder.withArgName("column").hasArg()
            .withDescription("Source column to check for incremental change").withLongOpt(INCREMENT_COL_ARG)
            .create());
        incrementalOpts.addOption(OptionBuilder.withArgName("value").hasArg()
            .withDescription("Last imported value in the incremental check column")
            .withLongOpt(INCREMENT_LAST_VAL_ARG).create());

        return incrementalOpts;
    }

    /**
     * Configure the command-line arguments we expect to receive
     */
    public void configureOptions(ToolOptions toolOptions) {

        toolOptions.addUniqueOptions(getCommonOptions());
        toolOptions.addUniqueOptions(getImportOptions());
        if (!allTables) {
            toolOptions.addUniqueOptions(getIncrementalOptions());
        }
        toolOptions.addUniqueOptions(getOutputFormatOptions());
        toolOptions.addUniqueOptions(getInputFormatOptions());
        toolOptions.addUniqueOptions(getHiveOptions(true));
        toolOptions.addUniqueOptions(getHBaseOptions());
        toolOptions.addUniqueOptions(getHCatalogOptions());
        toolOptions.addUniqueOptions(getHCatImportOnlyOptions());
        toolOptions.addUniqueOptions(getAccumuloOptions());

        // get common codegen opts.
        RelatedOptions codeGenOpts = getCodeGenOpts(allTables);

        // add import-specific codegen opts:
        codeGenOpts.addOption(OptionBuilder.withArgName("file").hasArg()
            .withDescription("Disable code generation; use specified jar").withLongOpt(JAR_FILE_NAME_ARG).create());

        toolOptions.addUniqueOptions(codeGenOpts);
    }

    @Override
    /** {@inheritDoc} */
    public void printHelp(ToolOptions toolOptions) {
        super.printHelp(toolOptions);
        System.out.println("");
        if (allTables) {
            System.out.println("At minimum, you must specify --connect");
        } else {
            System.out.println("At minimum, you must specify --connect and --table");
        }

        System.out.println("Arguments to mysqldump and other subprograms may be supplied");
        System.out.println("after a '--' on the command line.");
    }

    private void applyIncrementalOptions(CommandLine in, SqoopOptions out) throws InvalidOptionsException {
        if (in.hasOption(INCREMENT_TYPE_ARG)) {
            String incrementalTypeStr = in.getOptionValue(INCREMENT_TYPE_ARG);
            if ("append".equals(incrementalTypeStr)) {
                out.setIncrementalMode(SqoopOptions.IncrementalMode.AppendRows);
                // This argument implies ability to append to the same directory.
                out.setAppendMode(true);
            } else if ("lastmodified".equals(incrementalTypeStr)) {
                out.setIncrementalMode(SqoopOptions.IncrementalMode.DateLastModified);
            } else {
                throw new InvalidOptionsException("Unknown incremental import mode: " + incrementalTypeStr
                    + ". Use 'append' or 'lastmodified'." + HELP_STR);
            }
        }

        if (in.hasOption(INCREMENT_COL_ARG)) {
            out.setIncrementalTestColumn(in.getOptionValue(INCREMENT_COL_ARG));
        }

        if (in.hasOption(INCREMENT_LAST_VAL_ARG)) {
            out.setIncrementalLastValue(in.getOptionValue(INCREMENT_LAST_VAL_ARG));
        }
    }

    public void applyOptions(CommandLine in, SqoopOptions out) {

        try {
            applyCommonOptions(in, out);

            if (in.hasOption(DIRECT_ARG)) {
                out.setDirectMode(true);
            }

            if (!allTables) {
                if (in.hasOption(TABLE_ARG)) {
                    out.setTableName(in.getOptionValue(TABLE_ARG));
                }

                if (in.hasOption(COLUMNS_ARG)) {
                    String[] cols = in.getOptionValue(COLUMNS_ARG).split(",");
                    for (int i = 0; i < cols.length; i++) {
                        cols[i] = cols[i].trim();
                    }
                    out.setColumns(cols);
                }

                if (in.hasOption(SPLIT_BY_ARG)) {
                    out.setSplitByCol(in.getOptionValue(SPLIT_BY_ARG));
                }

                if (in.hasOption(SPLIT_LIMIT_ARG)) {
                    out.setSplitLimit(Integer.parseInt(in.getOptionValue(SPLIT_LIMIT_ARG)));
                }

                if (in.hasOption(WHERE_ARG)) {
                    out.setWhereClause(in.getOptionValue(WHERE_ARG));
                }

                if (in.hasOption(TARGET_DIR_ARG)) {
                    out.setTargetDir(in.getOptionValue(TARGET_DIR_ARG));
                }

                if (in.hasOption(APPEND_ARG)) {
                    out.setAppendMode(true);
                }

                if (in.hasOption(DELETE_ARG)) {
                    out.setDeleteMode(true);
                }

                if (in.hasOption(SQL_QUERY_ARG)) {
                    out.setSqlQuery(in.getOptionValue(SQL_QUERY_ARG));
                }

                if (in.hasOption(SQL_QUERY_BOUNDARY)) {
                    out.setBoundaryQuery(in.getOptionValue(SQL_QUERY_BOUNDARY));
                }

                if (in.hasOption(MERGE_KEY_ARG)) {
                    out.setMergeKeyCol(in.getOptionValue(MERGE_KEY_ARG));
                }

                applyValidationOptions(in, out);
            }

            if (in.hasOption(WAREHOUSE_DIR_ARG)) {
                out.setWarehouseDir(in.getOptionValue(WAREHOUSE_DIR_ARG));
            }

            if (in.hasOption(FMT_SEQUENCEFILE_ARG)) {
                out.setFileLayout(SqoopOptions.FileLayout.SequenceFile);
            }

            if (in.hasOption(FMT_TEXTFILE_ARG)) {
                out.setFileLayout(SqoopOptions.FileLayout.TextFile);
            }

            if (in.hasOption(FMT_AVRODATAFILE_ARG)) {
                out.setFileLayout(SqoopOptions.FileLayout.AvroDataFile);
            }

            if (in.hasOption(FMT_PARQUETFILE_ARG)) {
                out.setFileLayout(SqoopOptions.FileLayout.ParquetFile);
            }

            if (in.hasOption(NUM_MAPPERS_ARG)) {
                out.setNumMappers(Integer.parseInt(in.getOptionValue(NUM_MAPPERS_ARG)));
            }

            if (in.hasOption(MAPREDUCE_JOB_NAME)) {
                out.setMapreduceJobName(in.getOptionValue(MAPREDUCE_JOB_NAME));
            }

            if (in.hasOption(COMPRESS_ARG)) {
                out.setUseCompression(true);
            }

            if (in.hasOption(COMPRESSION_CODEC_ARG)) {
                out.setCompressionCodec(in.getOptionValue(COMPRESSION_CODEC_ARG));
            }

            if (in.hasOption(DIRECT_SPLIT_SIZE_ARG)) {
                out.setDirectSplitSize(Long.parseLong(in.getOptionValue(DIRECT_SPLIT_SIZE_ARG)));
            }

            if (in.hasOption(INLINE_LOB_LIMIT_ARG)) {
                out.setInlineLobLimit(Long.parseLong(in.getOptionValue(INLINE_LOB_LIMIT_ARG)));
            }

            if (in.hasOption(FETCH_SIZE_ARG)) {
                out.setFetchSize(new Integer(in.getOptionValue(FETCH_SIZE_ARG)));
            }

            if (in.hasOption(JAR_FILE_NAME_ARG)) {
                out.setExistingJarName(in.getOptionValue(JAR_FILE_NAME_ARG));
            }

            if (in.hasOption(AUTORESET_TO_ONE_MAPPER)) {
                out.setAutoResetToOneMapper(true);
            }

            applyIncrementalOptions(in, out);
            applyHiveOptions(in, out);
            applyCodeGenOptions(in, out, allTables);
            applyHBaseOptions(in, out);
            applyHCatalogOptions(in, out);
            applyAccumuloOptions(in, out);

        } catch (NumberFormatException nfe) {
            throw new InvalidOptionsException("Error: expected numeric argument.\n" + "Try --help for usage.");
        }
    }

    /**
     * Validate import-specific arguments.
     *
     * @param options the configured SqoopOptions to check
     */
    protected void validateImportOptions(SqoopOptions options) throws InvalidOptionsException {
        if (!allTables && options.getTableName() == null && options.getSqlQuery() == null) {
            throw new InvalidOptionsException("--table or --" + SQL_QUERY_ARG + " is required for import. "
                + "(Or use sqoop import-all-tables.)" + HELP_STR);
        } else if (options.getExistingJarName() != null && options.getClassName() == null) {
            throw new InvalidOptionsException(
                "Jar specified with --jar-file, but no " + "class specified with --class-name." + HELP_STR);
        } else if (options.getTargetDir() != null && options.getWarehouseDir() != null) {
            throw new InvalidOptionsException("--target-dir with --warehouse-dir are incompatible options." + HELP_STR);
        } else if (options.getTableName() != null && options.getSqlQuery() != null) {
            throw new InvalidOptionsException(
                "Cannot specify --" + SQL_QUERY_ARG + " and --table together." + HELP_STR);
        } else if (options.getSqlQuery() != null && options.getTargetDir() == null && options.getHBaseTable() == null
            && options.getHCatTableName() == null && options.getAccumuloTable() == null) {
            throw new InvalidOptionsException("Must specify destination with --target-dir. " + HELP_STR);
        } else if (options.getSqlQuery() != null && options.doHiveImport() && options.getHiveTableName() == null) {
            throw new InvalidOptionsException(
                "When importing a query to Hive, you must specify --" + HIVE_TABLE_ARG + "." + HELP_STR);
        } else if (options.getSqlQuery() != null && options.getNumMappers() > 1 && options.getSplitByCol() == null) {
            throw new InvalidOptionsException(
                "When importing query results in parallel, you must specify --" + SPLIT_BY_ARG + "." + HELP_STR);
        } else if (options.isDirect() && options.getFileLayout() != SqoopOptions.FileLayout.TextFile
            && options.getConnectString().contains("jdbc:mysql://")) {
            throw new InvalidOptionsException("MySQL direct import currently supports only text output format. "
                + "Parameters --as-sequencefile --as-avrodatafile and --as-parquetfile are not "
                + "supported with --direct params in MySQL case.");
        } else if (options.isDirect() && options.doHiveDropDelims()) {
            throw new InvalidOptionsException("Direct import currently do not support dropping hive delimiters,"
                + " please remove parameter --hive-drop-import-delims.");
        } else if (allTables && options.isValidationEnabled()) {
            throw new InvalidOptionsException("Validation is not supported for " + "all tables but single table only.");
        } else if (options.getSqlQuery() != null && options.isValidationEnabled()) {
            throw new InvalidOptionsException(
                "Validation is not supported for " + "free from query but single table only.");
        } else if (options.getWhereClause() != null && options.isValidationEnabled()) {
            throw new InvalidOptionsException(
                "Validation is not supported for " + "where clause but single table only.");
        } else if (options.getIncrementalMode() != SqoopOptions.IncrementalMode.None && options.isValidationEnabled()) {
            throw new InvalidOptionsException(
                "Validation is not supported for " + "incremental imports but single table only.");
        } else if ((options.getTargetDir() != null || options.getWarehouseDir() != null)
            && options.getHCatTableName() != null) {
            throw new InvalidOptionsException(
                "--hcatalog-table cannot be used " + " --warehouse-dir or --target-dir options");
        } else if (options.isDeleteMode() && options.isAppendMode()) {
            throw new InvalidOptionsException("--append and --delete-target-dir can" + " not be used together.");
        } else if (options.isDeleteMode() && options.getIncrementalMode() != SqoopOptions.IncrementalMode.None) {
            throw new InvalidOptionsException("--delete-target-dir can not be used" + " with incremental imports.");
        } else if (options.getAutoResetToOneMapper() && (options.getSplitByCol() != null)) {
            throw new InvalidOptionsException("--autoreset-to-one-mapper and" + " --split-by cannot be used together.");
        }
    }

    /**
     * Validate the incremental import options.
     */
    private void validateIncrementalOptions(SqoopOptions options) throws InvalidOptionsException {
        if (options.getIncrementalMode() != SqoopOptions.IncrementalMode.None
            && options.getIncrementalTestColumn() == null) {
            throw new InvalidOptionsException("For an incremental import, the check column must be specified "
                + "with --" + INCREMENT_COL_ARG + ". " + HELP_STR);
        }

        if (options.getIncrementalMode() == SqoopOptions.IncrementalMode.None
            && options.getIncrementalTestColumn() != null) {
            throw new InvalidOptionsException(
                "You must specify an incremental import mode with --" + INCREMENT_TYPE_ARG + ". " + HELP_STR);
        }

        if (options.getIncrementalMode() == SqoopOptions.IncrementalMode.DateLastModified
            && options.getFileLayout() == SqoopOptions.FileLayout.AvroDataFile) {
            throw new InvalidOptionsException("--" + INCREMENT_TYPE_ARG
                + " lastmodified cannot be used in conjunction with --" + FMT_AVRODATAFILE_ARG + "." + HELP_STR);
        }
    }

    @Override
    public void validateOptions(SqoopOptions options) {

        // If extraArguments is full, check for '--' followed by args for
        // mysqldump or other commands we rely on.
        options.setExtraArgs(getSubcommandArgs(extraArguments));
        int dashPos = getDashPosition(extraArguments);

        if (hasUnrecognizedArgs(extraArguments, 0, dashPos)) {
            throw new InvalidOptionsException(HELP_STR);
        }

        validateImportOptions(options);
        validateIncrementalOptions(options);
        validateCommonOptions(options);
        validateCodeGenOptions(options);
        validateOutputFormatOptions(options);
        validateHBaseOptions(options);
        validateHiveOptions(options);
        validateHCatalogOptions(options);
        validateAccumuloOptions(options);
    }

    @Override
    public int run(SqoopOptions options) {
        return 0;
    }
}
