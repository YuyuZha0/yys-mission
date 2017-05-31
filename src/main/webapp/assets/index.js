/**
 * Created by zhaoyy on 2016/12/21.
 */

(function ($, ctx) {
    'use strict';

    console.log("index.js loaded.");
    console.assert($, 'jQuery is required');

    const $queryInput = $('#query-input');
    const $nameInput = $('#name-input');
    const $emailInput = $('#email-input');
    const $msgInput = $('#msg-input');

    const $resultName = $('#result-name-span');
    const $resultImage = $('#character-image-div');
    const $resultDiv = $('#result-div');

    $queryInput.typeahead({
        source: function (query, process) {
            $.ajax({
                url: ctx + '/api/getAutoComplement',
                type: 'post',
                data: {
                    query: query
                }
            }).done(function (data) {
                if (data.status === 0)
                    process(data.data.queryList);
                else process([]);
            });
        },
        matcher: function () {
            return true;
        }
    });


    $('#query-btn').on('click', function () {
        const query = $queryInput.val();
        if (query == '')
            $queryInput.focus();
        $.ajax({
            url: ctx + '/api/getQueryResult',
            type: 'post',
            data: {
                query: query
            }
        }).done(function (data) {
            if (data.status === 0) {
                renderQueryResult(data.data.character, data.data.queryResultList);
                location.href = '#result';
            } else {
                modalAlert(data.msg);
            }
        });
    });

    const renderQueryResult = function (character, queryResultList) {
        console.assert($.isPlainObject(character) && $.isArray(queryResultList));
        $resultName.html(`查询结果&#58;<strong>${character.name}</strong>`);
        const img = `<a href="javascript:;" title="${character.name}">
                     <img src="${ctx}/images/characters/${character.imageName}" class="img-responsive wowload fadeInUp" alt="${character.name}"></a>`;
        $resultImage.html(img);
        let html = '';
        for (let i = 0, len = queryResultList.length; i < len; i++) {
            const a = queryResultList[i];
            html += `<h5>${a.locationName}&#58; <mark>数量&#58;${a.count}</mark></h5><ul>`;
            console.assert($.isArray(a.distributions));
            for (let j = 0, len1 = a.distributions.length; j < len1; j++) {
                const b = a.distributions[j];
                html += `<li>${b.battleName}->${b.roundName}&#58; ${b.characterName} &times; ${b.count}</li>`;
            }
            html += '</ul>';
        }
        $resultDiv.html(html);
    };

    $('#msg-btn').on('click', function () {
        const name = $nameInput.val();
        const email = $emailInput.val();
        const msg = $msgInput.val();
        if (msg == '')
            $msgInput.focus();
        $.ajax({
            url: ctx + '/api/leaveMsg',
            type: 'post',
            data: {
                name: name,
                email: email,
                msg: msg
            }
        }).done(function (data) {
            modalAlert(data.msg);
        });
    });

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
})(window.jQuery, CONTEXT);

