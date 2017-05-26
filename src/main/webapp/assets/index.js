/**
 * Created by zhaoyy on 2016/12/21.
 */

(function ($, ctx) {
    'use strict';

    console.log("index.js loaded.");
    console.assert($, 'jQuery is required');

    const modalAlert = function (msg, callback) {
        if (msg == '')
            return;
        $('#alert-msg').text(msg);
        $('#alertModal').modal('show', function (e) {
            if ($.isFunction(callback))
                callback();
            else
                console.error(callback + 'is not a function');
        });
    };
})(window.jQuery, echarts, CONTEXT);

