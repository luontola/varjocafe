$(function () {

    var areas = {};
    var restaurants = {};

    function updateRestaurantVisibilities() {
        Object.keys(restaurants).forEach(function (restaurantId) {
            var restaurant = restaurants[restaurantId];
            var area = areas[restaurant.areaId];
            var mode = 0;
            if (area.expanded) {
                mode = restaurant.expanded ? 1 : 2;
            }
            restaurant.expandedRow.style.display = ['none', null, 'none'][mode];
            restaurant.collapsedRow.style.display = ['none', 'none', null][mode];
        })
    }

    function toggleArea(areaId) {
        var area = areas[areaId];
        area.expanded = !area.expanded;
        console.log(area.expanded ? "Expand area" : "Collapse area", areaId);
        updateRestaurantVisibilities();
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

    $('.area-row').each(function (index, row) {
        var areaId = $(row).attr('data-area-id');
        areas[areaId] = {
            areaId: areaId,
            expanded: false,
            row: row
        };
        $(row).click(function () {
            toggleArea(areaId);
        });
    });

    $('.restaurant-row.collapsed').each(function (index, row) {
        var restaurantId = $(row).attr('data-restaurant-id');
        var areaId = $(row).prevAll('.area-row').attr('data-area-id')
        restaurants[restaurantId] = {
            restaurantId: restaurantId,
            areaId: areaId,
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

    updateRestaurantVisibilities();
});
