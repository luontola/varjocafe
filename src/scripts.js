$(function() {
    // TODO: show the price tool tips using HTML formatting
    $('span.price').removeAttr('title');

    $('<div style="text-align: right;" id="controls"></div>').insertBefore('#menus');
    var showPastLink = $('<a href="#">Näytä eilisen ruoat</a>').appendTo('#controls');
    var hidePastLink = $('<a href="#">Piilota eilisen ruoat</a>').appendTo('#controls');

    showPastLink.click(function(e) {
        $('.past').show();
        showPastLink.hide();
        hidePastLink.show();
        e.preventDefault();
    });
    hidePastLink.click(function(e) {
        $('.past').hide();
        showPastLink.show();
        hidePastLink.hide();
        e.preventDefault();
    });
    hidePastLink.click();
});
