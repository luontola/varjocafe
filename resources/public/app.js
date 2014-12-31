$(function () {

    function expandRestaurant(restaurantId) {
        console.log("Expand restaurant", restaurantId)
        $('.restaurant-row.collapsed[data-restaurant-id="' + restaurantId + '"]').css('display', 'none');
        $('.restaurant-row.expanded[data-restaurant-id="' + restaurantId + '"]').css('display', '');
    }

    function collapseRestaurant(restaurantId) {
        console.log("Collapse restaurant", restaurantId)
        $('.restaurant-row.collapsed[data-restaurant-id="' + restaurantId + '"]').css('display', '');
        $('.restaurant-row.expanded[data-restaurant-id="' + restaurantId + '"]').css('display', 'none');
    }

    $('.restaurant-row.collapsed').each(function (index, row) {
        var restaurantId = $(row).attr('data-restaurant-id');
        $(row).click(function () {
            expandRestaurant(restaurantId)
        });
    });

    $('.restaurant-row.expanded').each(function (index, row) {
        var restaurantId = $(row).attr('data-restaurant-id');
        $(row).click(function () {
            collapseRestaurant(restaurantId)
        });
    });
});
