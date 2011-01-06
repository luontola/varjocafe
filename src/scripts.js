// HTML formatted tool tips
$(function() {
    var installRichTooltip = function(index) {
        var titleHtml = $(this).attr('title');
        $(this).removeAttr('title');

        var reference = $(this);
        var tooltip = $('<div class="tooltip">' + titleHtml + '</div>').insertAfter(reference);

        var showTooltip = function(e) {
            tooltip.show();
            tooltip.position({
                my: 'left top',
                at: 'left bottom',
                of: reference,
                collision: 'fit flip'
            });
        };
        var hideTooltip = function(e) {
            tooltip.hide();
        };
        $(this).hover(showTooltip, hideTooltip);
    };

    $('[title^="<"]').each(installRichTooltip);
});

// Hide menus for past days by default; require user action to show them
$(function() {
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
