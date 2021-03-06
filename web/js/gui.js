var $;
var Ajax;
var swal;
var E = /** @class */ (function () {
    function E() {
    }
    E.$PAGE_LIST = $('#page_list');
    E.$NEIGHBORS = $('#neighbors');
    E.$MODULES = $('#ixis');
    E.$LOG = $('#log');
    return E;
}());
var Page = /** @class */ (function () {
    function Page(name) {
        this.name = name;
        this.identifier = name.toLowerCase().replace(/ /g, "_");
        this.$li = $('<li>').text(this.name.toUpperCase());
        var thisPage = this;
        this.$li.click(function (event) { Page.switch_to(thisPage); });
        this.$page = $('#page_' + this.identifier);
    }
    Page.init_pages = function () {
        $('.page').addClass("hidden");
        E.$PAGE_LIST.html("");
        this.NEIGHBORS.init();
        this.CONFIG.init();
        this.LOG.init();
        this.IXIS.init();
    };
    Page.prototype.init = function () {
        E.$PAGE_LIST.append(this.$li);
        this.$page.addClass("hidden").prepend($("<header>").html($("<h1>").text(this.name)));
    };
    Page.switch_to = function (page) {
        Page.current_page.$page.addClass("hidden");
        Page.current_page.$li.removeClass("highlighted");
        page.$page.removeClass("hidden");
        page.$li.addClass("highlighted");
        Page.current_page = page;
    };
    Page.NEIGHBORS = new Page("Neighbors");
    Page.CONFIG = new Page("Config");
    Page.LOG = new Page("Log");
    Page.IXIS = new Page("IXI Modules");
    Page.current_page = Page.NEIGHBORS;
    return Page;
}());
var Form = /** @class */ (function () {
    function Form() {
    }
    Form.save_config = function () {
        var config = Form.config;
        Object.keys(config).forEach(function (property) {
            var $input = $('#config_' + property);
            config[property] = $input.attr("type") === "checkbox" ? $input.is(":checked") : $input.val();
        });
        Ajax.INSTANCE.set_config(config, Form.load_config);
    };
    Form.load_config = function () {
        Ajax.INSTANCE.get_config(function (config) {
            Form.config = config;
            Form.put_config(config);
        });
    };
    Form.put_config = function (config) {
        Object.keys(config).forEach(function (property) {
            var $input = $('#config_' + property);
            $input.attr("type") === "checkbox" ? $input.prop("checked", config[property]) : $input.val(config[property]);
        });
    };
    return Form;
}());
var NeighborViewer = /** @class */ (function () {
    function NeighborViewer() {
    }
    NeighborViewer.load = function () {
        Ajax.INSTANCE.get_neighbor_stats(NeighborViewer.printNeighborStats);
    };
    NeighborViewer.printNeighborStats = function (response) {
        const neighbors = response['neighbors'];
        E.$NEIGHBORS.html("");
        var _loop_1 = function (i) {
            var neighbor = neighbors[i];
            var $stats = $("<div>").addClass("stats")
                .append($("<div>").addClass("all").text(neighbor['all']))
                .append($("<div>").addClass("new").text(neighbor['new']))
                .append($("<div>").addClass("requested").text(neighbor['requested']))
                .append($("<div>").addClass("invalid").text(neighbor['invalid']))
                .append($("<div>").addClass("ignored").text(neighbor['ignored']));
            var $neighbor = $("<div>").addClass("neighbor")
                .append($("<div>").addClass("name").text(neighbor['address']))
                .append($stats)
                .append($("<button>").text("remove").click(function () {
                Ajax.INSTANCE.remove_neighbor(neighbor['address']);
            }));
            E.$NEIGHBORS.append($neighbor);
        };
        for (var i = 0; i < neighbors.length; i++) {
            _loop_1(i);
        }
        ;
    };
    NeighborViewer.open_add_neighbor_dialog = function () {
        swal("Enter Neighbor Address", "Format: `IP:PORT` or `HOST:PORT`\n\nExample: `123.4.5.678:1337` or `example.org:1337`", {
            button: "add neighbor",
            content: "input"
        }).then(function (value) {
            if (value.length > 0)
                Ajax.INSTANCE.add_neighbor(value);
        });
    };
    return NeighborViewer;
}());
var ModuleViewer = /** @class */ (function () {
    function ModuleViewer() {
    }
    ModuleViewer.load = function () {
        Ajax.INSTANCE.get_modules(ModuleViewer.print_modules);
    };
    ModuleViewer.print_modules = function (response) {
        const modules = response['modules'];
        E.$MODULES.html("");
        var _loop_2 = function (i) {
            var module = modules[i];
            var path = module['path'];
            var $module = $("<div>").addClass("ixi")
                .append($("<div>").addClass("name").text(module['name'] ? module['name'] : path));
            if (module['gui_port'] >= 0) {
                var gui_href = module['gui_port'] == 0
                    ? window.location.protocol + "//" + window.location.host + "/modules/" + module['name'] + "/"
                    : window.location.protocol + "//" + window.location.hostname + ":" + module['gui_port'] + "/";
                $module.append($("<a>").attr("href", gui_href).attr("target", "_blank").append($("<button>").text("open")));
            }
            if (module['configurable'])
                $module.append($("<button>").text("configure").click(function () { ModuleViewer.on_config_button_clicked(path); }));
            if (module['repository'])
                $module.append($("<a>").attr("href", "https://github.com/" + module['repository']).attr("target", "_blank").append($("<button>").text("repository")));
            $module
                //   .append($("<button>").text("config"))
                .append($("<button>").text("uninstall").click(function () {
                Ajax.INSTANCE.uninstall_module(path);
            }));
            E.$MODULES.append($module);
        };
        for (var i = 0; i < modules.length; i++) {
            _loop_2(i);
        }
    };
    ModuleViewer.on_config_button_clicked = function (path) {
        Ajax.INSTANCE.get_module_config(path, function (config) { ModuleViewer.configure_module(path, config['config']); });
    };
    ModuleViewer.configure_module = function (path, config) {
        var textarea = document.createElement('textarea');
        textarea.style.fontSize = "10px";
        textarea.spellcheck = false;
        textarea.contentEditable = "true";
        textarea.style.height = "400px";
        textarea.value = JSON.stringify(config, null, '    ');
        textarea.className = 'swal-content__textarea';
        swal({
            title: 'module configuration',
            text: 'You are configuring `' + path + "`",
            content: textarea,
            buttons: {
                cancel: {
                    text: 'Cancel',
                    visible: true
                },
                confirm: {
                    text: 'Apply',
                    closeModal: false
                }
            }
        }).then(function (value) { if (value)
            Ajax.INSTANCE.set_module_config(path, textarea.value); });
    };
    ModuleViewer.open_install_module_dialog = function () {
        swal("Enter Github Repository", "Do not install modules from untrusted sources!\n\nFormat: `username/repository` or Github URL\n\nExample: `iotaledger/chat.ixi`", {
            button: "install module",
            content: "input",
        }).then(function (value) {
            if (value.length == 0)
                return;
            value = value.replace(/^(https:\/\/)?(github.com\/)/g, "");
            if (value.split("/").length != 2)
                return logError("'" + value + "' is does not match the required format 'username/repository'.", "Unexpected Format Repository");
            swal("Installing " + value + " ...", "Please wait while the download is in progress. Depending on the file size, this might take a while. If you don't reload the page, you will be notified once it's finished. In case of problems, check your Ict log.", "info");
            Ajax.INSTANCE.install_module(value);
        });
    };
    return ModuleViewer;
}());
var LogViewer = /** @class */ (function () {
    function LogViewer() {
    }
    LogViewer.load = function () {
        E.$LOG.html("");
        if (LogViewer.last_index == -1) // not yet initialized
            LogViewer.fetch_logs();
    };
    LogViewer.fetch_logs = function () {
        Ajax.INSTANCE.get_logs(LogViewer.last_index + 1, true, function (result) {
            LogViewer.last_index = result['max'];
            LogViewer.print_logs(result['logs']);
            LogViewer.fetch_logs();
        });
    };
    LogViewer.print_logs = function (logs) {
        logs.forEach(function (log) {
            var $log = $("<div>").addClass("log").addClass(log['level'])
                .append($("<div>").addClass("time").text(LogViewer.format_timestamp(log['timestamp'])))
                .append($("<div>").addClass("level").text(log['level'].padEnd(5)))
                .append($("<div>").addClass("message").text(log['message']));
            E.$LOG.append($log);
        });
    };
    LogViewer.format_timestamp = function (timestamp) {
        var date = new Date(timestamp);
        var months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
        var pad2 = function (n) { return n < 10 ? "0" + n : n; };
        return months[date.getMonth()] + "/" + pad2(date.getDate()) + " " + pad2(date.getHours()) + ":" + pad2(date.getMinutes()) + ":" + pad2(date.getSeconds());
    };
    LogViewer.last_index = -1;
    return LogViewer;
}());
function logError(message, title) {
    if (title === void 0) { title = "Whoops"; }
    swal(title, message, "error");
}
function logSuccess(message, title) {
    if (title === void 0) { title = "Success"; }
    swal(title, message, "success");
}
