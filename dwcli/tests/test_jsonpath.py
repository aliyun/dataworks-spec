import pytest
import json
from dwcli.pkg.jsonpath.manager import JSONPathManager, JSONPathError


class TestJSONPathManager:
    @pytest.fixture
    def sample_data(self):
        return {
            "version": "1.0",
            "spec": {
                "timeout": 3600,
                "nodes": [
                    {"id": "node1", "name": "Node 1"},
                    {"id": "node2", "name": "Node 2"}
                ],
                "config": {
                    "nested": {
                        "value": "test"
                    }
                }
            }
        }

    def test_get_value_simple(self, sample_data):
        result = JSONPathManager.get_value(sample_data, "version")
        assert result == "1.0"

    def test_get_value_nested(self, sample_data):
        result = JSONPathManager.get_value(sample_data, "spec.timeout")
        assert result == 3600

    def test_get_value_array(self, sample_data):
        result = JSONPathManager.get_value(sample_data, "spec.nodes[0].id")
        assert result == "node1"

    def test_get_value_deep_nested(self, sample_data):
        result = JSONPathManager.get_value(sample_data, "spec.config.nested.value")
        assert result == "test"

    def test_get_value_nonexistent(self, sample_data):
        result = JSONPathManager.get_value(sample_data, "nonexistent")
        assert result is None

    def test_set_value_simple(self, sample_data):
        result = JSONPathManager.set_value(sample_data, "version", "2.0", "string")
        assert result["version"] == "2.0"

    def test_set_value_nested(self, sample_data):
        result = JSONPathManager.set_value(sample_data, "spec.timeout", 7200, "int")
        assert result["spec"]["timeout"] == 7200

    def test_set_value_new_field(self, sample_data):
        result = JSONPathManager.set_value(sample_data, "spec.newfield", "newvalue")
        assert result["spec"]["newfield"] == "newvalue"

    def test_set_value_array_element(self, sample_data):
        result = JSONPathManager.set_value(sample_data, "spec.nodes[0].name", "Updated Node")
        assert result["spec"]["nodes"][0]["name"] == "Updated Node"

    def test_set_value_type_int(self, sample_data):
        result = JSONPathManager.set_value(sample_data, "spec.count", "42", "int")
        assert result["spec"]["count"] == 42

    def test_set_value_type_float(self, sample_data):
        result = JSONPathManager.set_value(sample_data, "spec.ratio", "3.14", "float")
        assert result["spec"]["ratio"] == 3.14

    def test_set_value_type_bool(self, sample_data):
        result = JSONPathManager.set_value(sample_data, "spec.enabled", "true", "bool")
        assert result["spec"]["enabled"] is True

    def test_set_value_type_json(self, sample_data):
        result = JSONPathManager.set_value(sample_data, "spec.metadata", '{"key": "value"}', "json")
        assert result["spec"]["metadata"] == {"key": "value"}

    def test_unset_value_simple(self, sample_data):
        result = JSONPathManager.unset_value(sample_data, "version")
        assert "version" not in result

    def test_unset_value_nested(self, sample_data):
        result = JSONPathManager.unset_value(sample_data, "spec.timeout")
        assert "timeout" not in result["spec"]

    def test_unset_value_nonexistent(self, sample_data):
        with pytest.raises(JSONPathError):
            JSONPathManager.unset_value(sample_data, "nonexistent")

    def test_infer_type_bool(self):
        assert JSONPathManager._infer_type("true") is True
        assert JSONPathManager._infer_type("false") is False

    def test_infer_type_int(self):
        assert JSONPathManager._infer_type("42") == 42

    def test_infer_type_float(self):
        assert JSONPathManager._infer_type("3.14") == 3.14

    def test_infer_type_string(self):
        assert JSONPathManager._infer_type("hello") == "hello"

    def test_infer_type_json(self):
        result = JSONPathManager._infer_type('{"key": "value"}')
        assert result == {"key": "value"}