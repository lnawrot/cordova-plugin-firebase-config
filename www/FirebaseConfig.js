var exec = require("cordova/exec");
var PLUGIN_NAME = "FirebaseConfig";

function promiseParameter(type, key) {
    return new Promise(function(resolve, reject) {
        exec(resolve, reject, PLUGIN_NAME, "get" + type, [key || ""]);
    });
}

module.exports = {
    init: function(defaultValues) {
        return new Promise(function(resolve, reject) {
            exec(resolve, reject, PLUGIN_NAME, "init", [defaultValues]);
        });
    },
    update: function(ttlSeconds) {
        return new Promise(function(resolve, reject) {
            exec(resolve, reject, PLUGIN_NAME, "fetch", [ttlSeconds || 0]);
        });
    },
    activate: function() {
        return new Promise(function(resolve, reject) {
            exec(resolve, reject, PLUGIN_NAME, "activate", []);
        });
    },
    fetchAndActivate: function() {
        return new Promise(function(resolve, reject) {
            exec(resolve, reject, PLUGIN_NAME, "fetchAndActivate", []);
        });
    },
    getBoolean: function(key) {
        return promiseParameter("Boolean", key);
    },
    getString: function(key) {
        return promiseParameter("String", key);
    },
    getNumber: function(key) {
        return promiseParameter("Number", key);
    },
    getBytes: function(key) {
        return promiseParameter("Bytes", key);
    }
};
