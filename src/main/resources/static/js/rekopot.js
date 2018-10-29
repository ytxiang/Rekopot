$.ajaxSetup({
    headers: {
        'X-CSRF-TOKEN': $("#csrf_token").attr("content")
    }
});

$(function () {
    $("[data-hide]").on("click", function(){
    	$("." + $(this).attr("data-hide")).hide();
    });
    fileUpload('ffsubmit');
    fileUpdate('refresher');
    fileExtract('extractor');
    fileDelete('trash');
    noteEdit('notes');
    noteSave('editor');
    noteCancel('cancel');
});

function enableButtons() {
    $('#ffsubmit').removeAttr('disabled');
    $('#forceful').removeAttr('disabled');
}

function disableButtons() {
    $('#ffsubmit').attr('disabled','disabled');
    $('#forceful').attr('disabled','disabled');
}

function hideAlert() {
    $('.alert').hide();
    $("#alert-text").text("");
}

function showAlert(message) {
    $("#alert-text").text(message);
    $('.alert').show();
}

function deactivate() {
	disableButtons();
	hideAlert();
}

function gonext(data) {
    if (data) {
	enableButtons();
        showAlert(data);
    } else {
        window.location.reload();
    }
}

function fileUpload(name) {
    $("#" + name).click(function (event) {
        event.preventDefault();
        var form = $('#fileform')[0];
        var file = $("#file")[0].files[0]
        var data = new FormData(form);
        if (file == null) {
            showAlert("No file to upload");
            return;
        }
        if (file.size > 10485760) {
            showAlert("Failed to upload! File size must be no more than 10MB!");
            return;
        }
	deactivate();
        $.ajax({
            type: 'POST',
            url: "/file/upload",
            data: data,
            cache: false,
            contentType: false,
            processData: false,
            success: gonext
        })
    })
}

function fileUpdate(name) {
    $("span[name='" + name + "']").click(function () {
        var form = $('#fileform')[0];
        var data = new FormData(form);
        var file = $("#file")[0].files[0];
        var msgstr = "No file has been selected.\n" +
                     "Are you sure to continue updating? " +
                     "Doing so will purge the original file."

        if (file == null && false == confirm(msgstr)) {
		return;
        }
        if (file != null && file.size > 10485760) {
            showAlert("Failed to upload! File size must be no more than 10MB!");
            return;
        }
       var opts = {
            type: 'POST',
            method: 'POST',
            url: $(this).attr("data"),
            data: data,
            cache: false,
            contentType: false,
            processData: false,
            success: gonext
        }
        /*
        // Old browser emulation
        if (data.fake) {
            // Make sure no text encoding stuff is done by xhr
            opts.xhr = function () {
                var xhr = jQuery.ajaxSettings.xhr();
                xhr.send = xhr.sendAsBinary;
                return xhr;
            }
            opts.contentType = "multipart/form-data; boundary=" + data.boundary;
            opts.data = data.toString();
        }
        */
	deactivate();
        $.ajax(opts);
    })
}

function fileExtract(name) {
    $("span[name='" + name + "']").click(function (e) {
	var force;
        if (false == confirm("Are you sure to overwrite existing description ?"))
            return;
	deactivate();
	force = (e.ctrlKey) ? 1 : 0;

        $.ajax({
            type: 'POST',
            url: $(this).attr("data"),
            data: {'force' : force},
            success: gonext
        })
    })
}

function fileDelete(name) {
    $("span[name='" + name + "']").click(function () {
        if (false == confirm("Are you sure to delete ?"))
            return;
	deactivate();
        $.ajax({
            type: 'DELETE',
            url: $(this).attr("data"),
            success: gonext
        })
    })
}

function noteEdit(name) {
    $("input[name='" + name + "']").on({
	dblclick : function () {
            this.readOnly='';
	},
	blur: function () {
	    this.readOnly='readonly';
	}
    });
}

function noteSave(name) {
    $("span[name='" + name + "']").click(function () {

        var old = $(this).parent().find("#cancel").attr("data");
        var now = $(this).parent().find("#notes").val();
        if (old == now)
            return;

        if (false == confirm("Are you sure to save changes?"))
            return;

	var formData = {
	    'notes' : $(this).parent().find("#notes").val()
	};

	deactivate();
        $.ajax({
            type: 'POST',
            data: formData,
            url: $(this).attr("data"),
            success: gonext
        })
    })
}

function noteCancel(name) {
    $("span[name='" + name + "']").click(function () {
        var notes = $(this).parent().find("#notes");
	notes.val($(this).attr("data"));
	notes.attr('readonly', true);
    })
}
