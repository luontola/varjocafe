$(function () {

    var restaurants = {};

    function updateRestaurantVisibilities() {
        Object.keys(restaurants).forEach(function (restaurantId) {
            var restaurant = restaurants[restaurantId];
            var expanded = restaurant.expanded;
            restaurant.expandedRow.style.display = expanded ? null : 'none';
            restaurant.collapsedRow.style.display = expanded ? 'none' : null;
        })
    }

    function expandArea(areaId) {
        console.log("Expand area", areaId);
    }

    function collapseArea(areaId) {
        console.log("Collapse area", areaId);
    }

    function expandRestaurant(restaurantId) {
        console.log("Expand restaurant", restaurantId);
        restaurants[restaurantId].expanded = true;
        updateRestaurantVisibilities();
    }

    function collapseRestaurant(restaurantId) {
        console.log("Collapse restaurant", restaurantId);
        restaurants[restaurantId].expanded = false;
        updateRestaurantVisibilities();
    }

    $('.restaurant-row.collapsed').each(function (index, row) {
        var restaurantId = $(row).attr('data-restaurant-id');
        restaurants[restaurantId] = {
            restaurantId: restaurantId,
            expanded: true,
            collapsedRow: row
        };
        $(row).click(function () {
            expandRestaurant(restaurantId);
        });
    });

    $('.restaurant-row.expanded').each(function (index, row) {
        var restaurantId = $(row).attr('data-restaurant-id');
        restaurants[restaurantId].expandedRow = row;
        $(row).click(function () {
            collapseRestaurant(restaurantId);
        });
    });
});
