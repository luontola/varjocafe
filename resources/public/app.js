"use strict";

$(function () {

    var areas = {};
    var restaurants = {};

    function updateRestaurantVisibilities() {
        var menusVisible = false;

        Object.keys(areas).forEach(function (areaId) {
            var area = areas[areaId];
            var mode = area.expanded ? 0 : 1;
            area.expandedRow.style.display = [null, 'none'][mode];
            area.collapsedRow.style.display = ['none', null][mode];
        });

        Object.keys(restaurants).forEach(function (restaurantId) {
            var restaurant = restaurants[restaurantId];
            var area = areas[restaurant.areaId];
            var mode = 2;
            if (area.expanded) {
                mode = restaurant.expanded ? 0 : 1;
            }
            restaurant.expandedRow.style.display = [null, 'none', 'none'][mode];
            restaurant.collapsedRow.style.display = ['none', null, 'none'][mode];
            menusVisible |= (mode == 0);
        });

        $('.date').css('visibility', menusVisible ? '' : 'hidden')
    }

    function expandArea(areaId) {
        console.log("Expand area", areaId);
        areas[areaId].expanded = true;
        updateRestaurantVisibilities();
    }

    function collapseArea(areaId) {
        console.log("Collapse area", areaId);
        areas[areaId].expanded = false;
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

    $('.area-row.collapsed').each(function (index, row) {
        var areaId = $(row).attr('data-area-id');
        areas[areaId] = {
            areaId: areaId,
            expanded: false,
            collapsedRow: row
        };
        $(row).find('.area-name').click(function () {
            expandArea(areaId);
        });
    });
    $('.area-row.expanded').each(function (index, row) {
        var areaId = $(row).attr('data-area-id');
        areas[areaId].expandedRow = row;
        $(row).find('.area-name').click(function () {
            collapseArea(areaId);
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
        $(row).find('.restaurant-name').click(function () {
            expandRestaurant(restaurantId);
        });
    });
    $('.restaurant-row.expanded').each(function (index, row) {
        var restaurantId = $(row).attr('data-restaurant-id');
        restaurants[restaurantId].expandedRow = row;
        $(row).find('.restaurant-name').click(function () {
            collapseRestaurant(restaurantId);
        });
    });

    updateRestaurantVisibilities();
});
