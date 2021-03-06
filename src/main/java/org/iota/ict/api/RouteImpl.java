package org.iota.ict.api;

import org.apache.logging.log4j.core.LogEvent;
import org.iota.ict.utils.LogAppender;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;

public abstract class RouteImpl implements Route {

    protected String path;
    protected JsonIct jsonIct;

    protected RouteImpl(JsonIct jsonIct, String path) {
        this.jsonIct = jsonIct;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            return execute(request).put("success", true);
        } catch (Throwable t) {
            t.printStackTrace();
            return error(t);
        }
    }

    public abstract JSONObject execute(Request request) throws Throwable;

    protected static JSONObject error(Throwable t) {
        return error(t.getMessage());
    }

    protected static JSONObject error(String message) {
        return new JSONObject().put("error", message).put("success", false);
    }
}

/**
 * @api {post} /getInfo/ GetInfo
 * @apiDescription  Provides general information about the Ict node.
 * @apiName GetInfo
 * @apiGroup General
 * @apiVersion 0.4.0
 * @apiSuccess {String} version Currently installed Ict version.
 * @apiSuccess {String} update Version of available update, unset if no update available.
 * @apiSuccess {Object} default_config Default Ict configuration. **Not the custom configuration of the node.**
 * @apiSuccessExample {json} Success-Response:
 *     {
 *         "version": "0.5.0"
 *         "update": "0.6.0",
 *         "default_config": {
 *            "name": "ict",
 *            "round_duration": 60000,
 *             ...
 *         }
 *         "success": true,
 *     }
 */
class RouteGetInfo extends RouteImpl {

    protected RouteGetInfo(JsonIct jsonIct) { super(jsonIct, "/getInfo"); }

    @Override
    public JSONObject execute(Request request) {
        return jsonIct.getInfo();
    }
}

/**
 * @api {post} /update/ Update
 * @apiParam {String} version Version to update to. Use the version provided in the `update` field returned by [GetInfo](#api-General-GetInfo).
 * @apiDescription Downloads the latest Ict version.
 * @apiName Update
 * @apiGroup General
 * @apiVersion 0.4.0
 * @apiSuccess {Object} success `true` if the action was successful, `false` if there was an error.
 * @apiSuccessExample {json} Success-Response:
 *     { "success": true }
 */
class RouteUpdate extends RouteImpl {

    protected RouteUpdate(JsonIct jsonIct) { super(jsonIct, "/update"); }

    @Override
    public JSONObject execute(Request request) throws IOException {
        String version = request.queryParams("version");
        return jsonIct.update(version);
    }
}
/**
 * @api {post} /getConfig/ GetConfig
 * @apiDescription Provides the current configuration of the running Ict node instance.
 * @apiName GetConfig
 * @apiGroup Config
 * @apiVersion 0.4.0
 * @apiSuccess {Object} config Current Ict node configuration.
 * @apiSuccessExample {json} Success-Response:
 *     {
 *         "config": {
 *              "name": "myIct",
 *              "round_duration": 300000,
 *               ...
 *         }
 *         "success": true,
 *     }
 */
class RouteGetConfig extends RouteImpl {

    protected RouteGetConfig(JsonIct jsonIct) { super(jsonIct, "/getConfig"); }

    @Override
    public JSONObject execute(Request request) {
        return jsonIct.getConfig();
    }
}

/**
 * @api {post} /setConfig/ SetConfig
 * @apiParam {Object} config The complete configuration object to apply. Must have the same stricture as `config` field returned by [GetConfig](#api-Config-GetConfig).
 * @apiDescription Changes the configuration of the running node instance. The new configuration will be stored in ict.cfg.
 * Depending on the exact changes, the node might be restarted to apply the changes.
 * @apiName SetConfig
 * @apiGroup Config
 * @apiVersion 0.4.0
 * @apiSuccess {Object} success `true` if the action was successful, `false` if there was an error.
 * @apiSuccessExample {json} Success-Response:
 *     {"success": true }
 */
class RouteSetConfig extends RouteImpl {

    protected RouteSetConfig(JsonIct jsonIct) { super(jsonIct, "/setConfig"); }

    public JSONObject execute(Request request) {
        String configString = request.queryParams("config");
        return jsonIct.setConfig(configString);
    }
}

/**
 * @api {post} /getNeighbors/ GetNeighbors
 * @apiDescription Returns all neighbors the Ict node is currently connected to. Also includes stats of the most recent rounds.
 * @apiName GetNeighbors
 * @apiGroup Neighbors
 * @apiVersion 0.4.0
 * @apiSuccess {Array} neighbors Array of all neighbors. For each neighbor, contains the node `address` and transaction `stats` of the previous rounds.
 * @apiSuccessExample {json} Success-Response:
 *     {
 *         "neighbors": [
 *             {
 *                 "address": "http://example.org:1337",
 *                 "stats": [
 *                     {"timestamp": 1547437313, "all": 192, new": 76, "requested": 3, "invalid": 0, "ignored": 5},
 *                     ...
 *                 ]
*              },
 *             ...
 *         ]
 *         "success": true
 *     }
 */
class RouteGetNeighbors extends RouteImpl {

    protected RouteGetNeighbors(JsonIct jsonIct) { super(jsonIct, "/getNeighbors"); }

    public JSONObject execute(Request request) {
        return new JSONObject().put("neighbors", jsonIct.getNeighbors());
    }
}

/**
 * @api {post} /addNeighbor/ AddNeighbor
 * @apiParam {String} address Address of the neighbor node. Format: `HOST:IP` (e.g. `example.org:1337`).
 * @apiDescription Adds a neighbor to the current Ict instance.
 * @apiName AddNeighbor
 * @apiGroup Neighbors
 * @apiVersion 0.4.0
 * @apiSuccess {Object} success `true` if the action was successful, `false` if there was an error.
 * @apiSuccessExample {json} Success-Response:
 *     {"success": true }
 * @apiErrorExample {json} Format Error
 *     {"success": false, "error": "Address does not match required format 'host:port'." }
 * @apiErrorExample {json} TooManyNeighbors Error
 *     {"success": false, "error": "Already reached maximum amount of neighbors." }
 * */
class RouteAddNeighbor extends RouteImpl {

    protected RouteAddNeighbor(JsonIct jsonIct) { super(jsonIct, "/addNeighbor"); }

    public JSONObject execute(Request request) {
        String address = request.queryParams("address");
        return jsonIct.addNeighbor(address);
    }
}

/**
 * @api {post} /removeNeighbor/ RemoveNeighbor
 * @apiParam {String} address Address of the neighbor node to remove. Format: `HOST:IP` (e.g. `example.org:1337`).
 * @apiDescription Removes a neighbor from the current Ict instance.
 * @apiName RemoveNeighbor
 * @apiGroup Neighbors
 * @apiVersion 0.4.0
 * @apiSuccess {Object} success `true` if the action was successful, `false` if there was an error.
 * @apiSuccessExample {json} Success-Response:
 *     {"success": true }
 * @apiErrorExample {json} NeighborNotFound Error
 *     {"success": false, "error": "No neighbor with address 'example.org:1337'." }
 * */
class RouteRemoveNeighbor extends RouteImpl {

    protected RouteRemoveNeighbor(JsonIct jsonIct) { super(jsonIct, "/removeNeighbor"); }

    public JSONObject execute(Request request) {
        String address = request.queryParams("address");
        return jsonIct.removeNeighbor(address);
    }
}

/**
 * @api {post} /addModule/ AddModule
 * @apiParam {String} repository GitHub reference to the repository from where to download the module. Format: `username/repository` (e.g. `iotaledger/chat.ixi`)
 * @apiDescription Installs an IXI module by downloading the precompiled .jar file from GitHub.
 * @apiName AddModule
 * @apiGroup Modules
 * @apiVersion 0.4.0
 * @apiSuccess {Object} success `true` if the action was successful, `false` if there was an error.
 * @apiSuccessExample {json} Success-Response:
 *     {"success": true }
 * @apiErrorExample {json} NoReleasesFound Error
 *     {"success": false, "error": "No releases in repository iotaledger/chat.ixi" }
 * @apiErrorExample {json} NoAssetsFound Error
 *     {"success": false, "error": "No assets found in release '1.0'" }
 * */
class RouteAddModule extends RouteImpl {

    protected RouteAddModule(JsonIct jsonIct) { super(jsonIct, "/addModule"); }

    public JSONObject execute(Request request) {
        String repository = request.queryParams("user_slash_repo");
        return jsonIct.addModule(repository);
    }
}

/**
 * @api {post} /removeModule/ RemoveModule
 * @apiParam {String} path Relative path of the module in the modules/ directory. Use the value of the `path` field returned by [GetModules](#api-Modules-GetModules).
 * @apiDescription Terminates and deletes an installed IXI module.
 * @apiName RemoveModule
 * @apiGroup Modules
 * @apiVersion 0.4.0
 * @apiSuccess {Object} success `true` if the action was successful, `false` if there was an error.
 * @apiSuccessExample {json} Success-Response:
 *     {"success": true }
 * @apiErrorExample {json} ModuleNotFound Error
 *     {"success": false, "error": "No module 'chat.ixi.jar' installed." }
 * */
class RouteRemoveModule extends RouteImpl {

    protected RouteRemoveModule(JsonIct jsonIct) { super(jsonIct, "/removeModule"); }

    public JSONObject execute(Request request) {
        String path = request.queryParams("path");
        return jsonIct.removeModule(path);
    }
}

/**
 * @api {post} /updateModule/ UpdateModule
 * @apiParam {String} path Relative path of the module in the modules/ directory. Use the value of the `path` field returned by [GetModules](#api-Modules-GetModules).
 * @apiParam {String} version Version to install, use the value of the `update` field returned by [GetModules](#api-Modules-GetModules).
 * @apiDescription Installs a different version of the module. Deletes the current version.
 * @apiName UpdateModule
 * @apiGroup Modules
 * @apiVersion 0.4.0
 * @apiSuccess {Object} success `true` if the action was successful, `false` if there was an error.
 * @apiSuccessExample {json} Success-Response:
 *     {"success": true }
 * @apiErrorExample {json} ModuleNotFound Error
 *     {"success": false, "error": "No module 'chat.ixi.jar' installed." }
 * */
class RouteUpdateModule extends RouteImpl {

    protected RouteUpdateModule(JsonIct jsonIct) { super(jsonIct, "/updateModule"); }

    public JSONObject execute(Request request) throws Throwable {
        String path = request.queryParams("path");
        String version = request.queryParams("version");
        return jsonIct.updateModule(path, version);
    }
}

/**
 * @api {post} /getModules/ GetModules
 * @apiDescription Provides a list of all installed IXI modules.
 * @apiName GetModules
 * @apiGroup Modules
 * @apiVersion 0.4.0
 * @apiSuccess {Array} modules Array of all installed IXI modules. For each module, contains the `path` (identifier) and meta data.
 * The field `update` contains the version of an available update, if set.). If `configurable` is set to `true`, the
 * module can be configured. Please see [GetModuleConfig](#api-Modules-GetModuleConfig).
 * @apiSuccessExample {json} Success-Response:
 *     {
 *         "modules": [
 *             {
 *                 "path": "chat.ixi.jar",
 *                 "name": "CHAT.ixi",
 *                 "description": "...",
 *                 ...
 *                 "update": "1.3",
 *                 "configurable": true
*              },
 *             ...
 *         ],
 *         "success": true
 *     }
 * @apiErrorExample {json} ModuleNotFound Error
 *     {"success": false, "error": "No module 'chat.ixi' installed." }
 * */
class RouteGetModules extends RouteImpl {

    protected RouteGetModules(JsonIct jsonIct) { super(jsonIct, "/getModules"); }

    public JSONObject execute(Request request) {
        return new JSONObject().put("modules", jsonIct.getModules());
    }
}

/**
 * @api {post} /getModuleConfig/ getModuleConfig
 * @apiParam {String} path Relative path of the module in the modules/ directory. Use the value of the `path` field returned by [GetModules](#api-Modules-GetModules).
 * @apiDescription Returns the current and the default configuration of this IXI module. Unset if the module is not configurable.
 * @apiName GetModuleConfig
 * @apiGroup Modules
 * @apiVersion 0.5.0
 * @apiSuccess {Object} config The current configuration of the IXI module.
 * @apiSuccess {Object} default_config The default configuration. Intended for resetting the configuration.
 * @apiSuccessExample {json} Success-Response:
 *     {
 *         "config": { "color": "red", ... },
 *         "default_config": { "color": "blue", ... },
 *         "success": true
 *     }
 * @apiErrorExample {json} ModuleNotFound Error
 *     {"success": false, "error": "No module 'chat.ixi.jar'." }
 * */
class RouteGetModuleConfig extends RouteImpl {

    protected RouteGetModuleConfig(JsonIct jsonIct) { super(jsonIct, "/getModuleConfig"); }

    public JSONObject execute(Request request) {
        String path = request.queryParams("path");
        return jsonIct.getModuleConfig(path);
    }
}

/**
 * @api {post} /setModuleConfig/ setModuleConfig
 * @apiParam {String} path Relative path of the module in the modules/ directory. Use the value of the `path` field returned by [GetModules](#api-Modules-GetModules).
 * @apiParam {Object} config The new configuration object, usually a modified version what was returned by [GetModuleConfig](#api-Modules-GetModuleConfig).
 * @apiDescription Changes the configuration of the module. The new configuration will be stored in the respective module's cfg file.
 * @apiName SetModuleConfig
 * @apiGroup Modules
 * @apiVersion 0.5.0
 * @apiSuccess {Object} success `true` if the action was successful, `false` if there was an error.
 * @apiSuccessExample {json} Success-Response:
 *     {"success": true }
 * @apiErrorExample {json} ModuleNotFound Error
 *     {"success": false, "error": "No module 'chat.ixi.jar'." }
 * @apiErrorExample {json} ModuleNotFound Error
 *     {"success": false, "error": "Property 'color' cannot be assigned to value 'helicopter'." }
 * */
class RouteSetModuleConfig extends RouteImpl {

    protected RouteSetModuleConfig(JsonIct jsonIct) { super(jsonIct, "/setModuleConfig"); }

    public JSONObject execute(Request request) {
        String path = request.queryParams("path");
        JSONObject newConfig = new JSONObject(request.queryParams("config"));
        return jsonIct.setModuleConfig(path, newConfig);
    }
}

/**
 * @api {post} /getLogs/ GetLogs
 * @apiDescription Returns all log messages of the node within a specified index interval. Will not return the entire interval if it is too large.
 * @apiParam {Number} [min] Index of first message to include in response.
 * @apiParam {Number} [max] Index of last message to include in response.
 * @apiSuccess {Boolean} [block} If true, will wait until the at least the log message with the index matching the `min` parameter is available before responding.
 * @apiName GetLogs
 * @apiGroup Log
 * @apiVersion 0.5.0
 * @apiSuccess {Array} logs Array (limited length) of logs ordered by index (ascending).
 * @apiSuccess {Number} min Index of first available log message to fetch.
 * @apiSuccess {Number} max Index of last available log message to fetch.
 * @apiSuccessExample {json} Success-Response:
 *     {
 *         "logs": [
 *             {"level": "info", "timestamp": 1547437313, "message": "Sender/Neighbor]   102  |90   |0    |0    |0       localhost/127.0.0.1:14265"},
 *             {"level": "warn", "timestamp": 1547439294, "message": "[Receiver/Ict]   Received invalid transaction from neighbor: localhost/127.0.0.1:14265 (issuance timestamp not in tolerated interval)"},
 *             ...
 *         ],
 *         "min": 0,
 *         "max": 112,
 *         "success": true
 *     }
 * */
class RouteGetLogs extends RouteImpl {

    protected RouteGetLogs(JsonIct jsonIct) { super(jsonIct, "/getLogs"); }

    public JSONObject execute(Request request) throws InterruptedException {
        int min = Integer.parseInt(request.queryParamOrDefault("min", ""+LogAppender.getIndexMin()));
        boolean block = Boolean.parseBoolean(request.queryParamOrDefault("block", "false"));

        synchronized (LogAppender.NOTIFY_SYNCHRONIZER) {
            while (block && min > LogAppender.getIndexMax()) {
                LogAppender.NOTIFY_SYNCHRONIZER.wait();
            }
        }

        int max = Math.min(Integer.parseInt(request.queryParamOrDefault("max", ""+LogAppender.getIndexMax())), min+1000);

        JSONArray logs = new JSONArray();
        for(int i = min; i <= max; i++) {
            LogEvent event = LogAppender.getLogEvent(i);
            if(event != null)
                logs.put(logEventToJSON(i, event));
        }

        return new JSONObject().put("logs", logs).put("min", LogAppender.getIndexMin()).put("max", LogAppender.getIndexMax());
    }

    protected static JSONObject logEventToJSON(int index, LogEvent event) {
        return new JSONObject().put("index", index).put("message", event.getMessage().getFormattedMessage()).put("timestamp", event.getTimeMillis()).put("level", event.getLevel());
    }
}