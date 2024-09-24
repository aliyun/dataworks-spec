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

package com.aliyun.dataworks.migrationx.transformer.core.spark.command;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Encapsulates a connection between a launcher server and client. This takes care of the communication (sending and
 * receiving messages), while processing of messages is left for the implementations.
 */
abstract class LauncherConnection implements Closeable, Runnable {

    private static final Logger LOG = Logger.getLogger(LauncherConnection.class.getName());

    private final Socket socket;
    private final ObjectOutputStream out;

    private volatile boolean closed;

    LauncherConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.closed = false;
    }

    protected abstract void handle(LauncherProtocol.Message msg) throws IOException;

    @Override
    public void run() {
        try {
            FilteredObjectInputStream in = new FilteredObjectInputStream(socket.getInputStream());
            while (isOpen()) {
                LauncherProtocol.Message msg = (LauncherProtocol.Message)in.readObject();
                handle(msg);
            }
        } catch (EOFException eof) {
            // Remote side has closed the connection, just cleanup.
            try {
                close();
            } catch (Exception unused) {
                // no-op.
            }
        } catch (Exception e) {
            if (!closed) {
                LOG.log(Level.WARNING, "Error in inbound message handling.", e);
                try {
                    close();
                } catch (Exception unused) {
                    // no-op.
                }
            }
        }
    }

    protected synchronized void send(LauncherProtocol.Stop msg) throws IOException {
        try {
            CommandBuilderUtils.checkState(!closed, "Disconnected.");
            out.writeObject(msg);
            out.flush();
        } catch (IOException ioe) {
            if (!closed) {
                LOG.log(Level.WARNING, "Error when sending message.", ioe);
                try {
                    close();
                } catch (Exception unused) {
                    // no-op.
                }
            }
            throw ioe;
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (isOpen()) {
            closed = true;
            socket.close();
        }
    }

    boolean isOpen() {
        return !closed;
    }

}
