/* OLB transfers page behaviour.  Requires jQuery 1.7.x.  Do not upgrade jQuery without a full IE7 regression (OLB-1540). */
var OLB = OLB || {};

OLB.ctx = '';

OLB.refreshSummary = function () {
  var $f = $('#xfrForm');
  var isExt = $('#toAcctId option:selected').attr('data-ext') === 'Y' || $('#fromAcctId option:selected').attr('data-ext') === 'Y';
  if (isExt) { $('#deliveryRow').show(); } else { $('#deliveryRow').hide(); }
  $.ajax({
    url: OLB.ctx + '/secure/quote.do',
    type: 'POST',
    data: $f.serialize(),
    dataType: 'json',
    cache: false,
    success: function (q) {
      if (q.ok) {
        $('#sFrom').text(q.from); $('#sTo').text(q.to); $('#sAmount').text(q.amount);
        $('#sFee').text(q.fee); $('#sType').text(q.type); $('#sDelivery').text(q.delivery);
        $('#sTotal').text(q.total); $('#sTier').text(q.tier);
        $('#quoteErr').hide().text('');
      } else {
        $('#quoteErr').text(q.message).show();
      }
    },
    error: function (xhr) {
      if (xhr.status === 302 || xhr.status === 0) { window.location = OLB.ctx + '/index.jsp?expired=1'; }
    }
  });
};

$(function () {
  OLB.ctx = $('body').attr('data-ctx') || '';
  $('#cookie .close').click(function () { $('#cookie').hide(); });
  $('#schedToggle').click(function (e) { e.preventDefault(); $('#schedOpts').toggle(); });
  if ($('#xfrForm').length) {
    $('#xfrForm select, #xfrForm input').bind('change keyup', function () {
      clearTimeout(OLB._t);
      OLB._t = setTimeout(OLB.refreshSummary, 250);
    });
    OLB.refreshSummary();
  }
  // legacy double-submit guard
  $('form').submit(function () {
    var $b = $(this).find('button[type=submit]');
    if ($b.data('busy')) return false;
    $b.data('busy', true);
    setTimeout(function () { $b.data('busy', false); }, 4000);
    return true;
  });
});
