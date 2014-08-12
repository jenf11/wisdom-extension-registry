$(document).ready(function () {
    // Table Sorter.
    $("#table").tablesorter(
        {
            // We have overridden this style.
            theme: 'dropbox',

            sortList: [
                [0, 0],
                [1, 0]

            ],

            // header layout template; {icon} needed for some themes
            headerTemplate: '{content}&nbsp;{icon}',

            // initialize column styling of the table
            widgets: ["columns"],
            widgetOptions: {
                // change the default column class names
                // primary is the first column sorted, secondary is the second, etc
                columns: [ "primary", "secondary" ]
            }
        });
    //Tooltip
    $("a").tooltip();
    $("#refresh").click(function () {
        load();
        $("#error-msg").html("").removeClass("alert-success").removeClass("alert-danger");
    });
    //add a new extension
    $("#add").click(function () {
        create($('#url').val());
        $("#error-msg").html("").removeClass("alert-success").removeClass("alert-danger");
        $("#url").val("");

    });
    //load extension list
    load();

});

/* create a new extension from a json file, calls the upload method*/
function create(ext) {
    var url = "http://" + window.location.host + "/upload";
    // var url = /*[[${#routes.route('upload')}]]*/ null;
    $.ajax({
        type: "POST",
        url: url,
        data: { url: ext }
    }).done(function (data) {
        console.log(data);
        //if error message display
        if (data.error) {
            $("#error-msg").html(data.error + " " + data.reason).addClass("alert-danger").removeClass("alert-success");
        }
        else {
            $("#error-msg").html("The " + data.name + " extension has been added.").removeClass("alert-danger").addClass("alert-success");
        }
        load();
    });
}

/*remove the selected extension based on the database id number */
function remove(ext) {
    $.ajax({
        url: "http://" + window.location.host + "/list/" + encodeURIComponent(ext),
        type: 'DELETE',
        complete: function (result) {
            load();
        }
    });
}

/*create actions available for each extension so far that iis just delete, based on the database id number*/
function getActionBarForExtension(ext) {
    var bar = $("<div></div>").addClass("bundle-action-bar pull-right").addClass("btn-toolbar").attr("role",
        "toolbar");
    var inner = $("<div></div>").addClass("btn-group");
    var uninstall = $("<button type=\"button\" class=\"btn btn-default btn-xs\"><span class=\"glyphicon glyphicon-remove\"></span></button>");
    uninstall.click(function () {
        remove(ext)
    });
    inner.append(uninstall);
    bar.append(inner);
    return $("<td></td>").append(bar);
}

/* list all available extension in a table */
function writeExtensionData(data) {
    $("#ext-table-body").empty();
    $.each(data, function (index, ext) {
        var ename = ext.name;
        if (HasSubstring(ext.name, ".")) {
            ename = ename.replace(".", "-");
        }

        //create a new row using the extensions key as a collasable link
        var tr = $("<tr></tr>");
        var info = $("<td></td>");
        info.append($("<a></a>")
            .attr("href", "#collapse" + ename)
            .attr("data-toggle", "collapse")
            .html(ext.name));

        //create a list of the information for each key
        var list = $("<ul></ul>").toggleClass("properties");
        $.each(ext, function (key, value) {
            if (value != null && value != "undefined" && !(key == "date" || key == "id")) {
                if (key == "homepage") {
                    var href = ($("<a></a>")
                        .attr("href", value)
                        .html(value));
                    $(list).append($("<li></li>").append("<strong>" + key + ":</strong>&nbsp;").append(href));
                }
                else if (key == "repository" || key == "license") {
                    if (value.url != null && value.url != "undefined") {
                        var href = ($("<a></a>")
                            .attr("href", value.url)
                            .html(value.url));
                        $(list).append($("<li></li>").append("<strong>" + key + ":</strong>&nbsp;").append(href));
                    }
                }
                else {
                    $(list).append($("<li><strong>" + key + ":</strong>&nbsp;" + value + "</li>"));
                }
            }
        });

        info.append($("<div></div>")
            .attr("id", "collapse" + ename)
            .addClass("collapse", "meta")
            .html(list));
        //second column of table contains the version
        var version = $("<td></td>").html(ext.version + " (last updated " + ext.date + ")");

        if (hasAccessToActionBar) {
            $(tr).append(info).append(version).append(getActionBarForExtension(ext.id));
        }
        else {
            $(tr).append(info).append(version);
        }
        $("#ext-table-body").append(tr);
    });
    $("#ext-count").html(data.length);

    $("#filter").val("");
    $('table').trigger("update").filterTable({ // apply filterTable to all tables on this page
        filterSelector: '#filter',
        minRows: 2   //min number of rows before we filter
    });
}

/*load the list of extensions as json */
function load() {
    $.get("http://" + window.location.host + "/list").success(writeExtensionData)
}

/**
 * @return {boolean}
 */
function HasSubstring(string, substring) {
    return string.indexOf(substring) > -1;

}
