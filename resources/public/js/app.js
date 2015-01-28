"use strict";

$(function () {

    var areas = {};
    var restaurants = {};

    $.cookie.json = true;

    function loadState() {
        var state = $.cookie('state');
        if (!state) {
            return;
        }
        try {
            state.areas.forEach(function (unsafe) {
                var area = areas[unsafe.areaId];
                if (area) {
                    area.expanded = !!unsafe.expanded;
                }
            });
            state.restaurants.forEach(function (unsafe) {
                var restaurant = restaurants[unsafe.restaurantId];
                if (restaurant) {
                    restaurant.expanded = !!unsafe.expanded;
                }
            });
        } catch (e) {
            console.log('Failed to load state', e);
        }
    }

    function saveState() {
        var state = {
            areas: Object.keys(areas).map(function (areaId) {
                var area = areas[areaId];
                return {
                    areaId: area.areaId,
                    expanded: area.expanded
                };
            }),
            restaurants: Object.keys(restaurants).map(function (restaurantId) {
                var restaurant = restaurants[restaurantId];
                return {
                    restaurantId: restaurant.restaurantId,
                    expanded: restaurant.expanded
                };
            })
        };
        $.cookie('state', state, {expires: 365, path: '/'});
    }

    function updateRestaurantVisibilities() {
        var menusVisible = false;

        Object.keys(areas).forEach(function (areaId) {
            var area = areas[areaId];
            var mode = area.expanded ? 0 : 1;
            area.expandedRow.style.display = ['', 'none'][mode];
            area.collapsedRow.style.display = ['none', ''][mode];
            menusVisible |= area.expanded;
        });

        Object.keys(restaurants).forEach(function (restaurantId) {
            var restaurant = restaurants[restaurantId];
            var area = areas[restaurant.areaId];
            var mode = 2;
            if (area.expanded) {
                mode = restaurant.expanded ? 0 : 1;
            }
            restaurant.expandedRow.style.display = ['', 'none', 'none'][mode];
            restaurant.collapsedRow.style.display = ['none', '', 'none'][mode];
        });

        $('.date').css('visibility', menusVisible ? '' : 'hidden');
        saveState();
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
        $(row).click(function () {
            expandArea(areaId);
        });
    });
    $('.area-row.expanded').each(function (index, row) {
        var areaId = $(row).attr('data-area-id');
        areas[areaId].expandedRow = row;
        $(row).click(function () {
            collapseArea(areaId);
        });
    });

    $('.restaurant-row.collapsed').each(function (index, row) {
        var restaurantId = $(row).attr('data-restaurant-id');
        var areaId = $(row).prevAll('.area-row').attr('data-area-id');
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
        $(row).find('.restaurant-name').click(function () {
            collapseRestaurant(restaurantId);
        });
    });

    loadState();
    updateRestaurantVisibilities();
});
