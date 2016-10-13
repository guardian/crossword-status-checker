function checkIfCrosswordReadyToPublish(type, id) {
   var response = UrlFetchApp.fetch("<STATUS-CHECKER-URL>/PROD/get-status?type="+ type + "&id="+ id);
  var json = JSON.parse(response.getContentText());
  var inCapiPreviewBool = Boolean(json.apiStatus.inCapiPreview)
  if(inCapiPreviewBool == true) {
    return 'Yes'
  } else {
    return 'No'; 
  }
}


var baseMondayQuick = {
    'date': new Date(2016, 8,12),
    'no': 14460
}

var baseMondayCryptic = {
    'date': new Date(2016,8,5),
    'no': 26981
}

var baseSundaySpeedy = {
    'date': new Date(2016,8,4),
    'no': 1093
}

var baseSaturdayPrize = {
    'date': new Date(2016,8,3),
    'no': 26980
}

var baseMondayQuiptic = {
    'date': new Date(2016,8,5),
    'no': 877
}

function dayDiff(first, second) {
    return Math.round((second-first)/(1000*60*60*24));
}

function quickForDate(date) {
    if (date.getDay() === 0) {
        return '-'
    } else {
        var dayDifference = dayDiff(baseMondayQuick.date, date);
        var numberOfSundays = Math.floor(dayDifference/7)
        var xWordNo = baseMondayQuick.no + dayDifference - numberOfSundays 
        return xWordNo;
    }
}

function crypticForDate(date) {
    if (date.getDay() === 0 || date.getDay() === 6) {
        return '-';
    } else {
        var dayDifference = dayDiff(baseMondayCryptic.date, date);
        var numberOfSundays = Math.floor(dayDifference/7)
        var xWordNo = baseMondayCryptic.no + dayDifference - numberOfSundays;
        return xWordNo;
    }
}

function getNoForWeeklyXword(date, baseXword, publicationDayOfWeek) {
    if (date.getDay() != publicationDayOfWeek) {
        return '-';
    } else {
        var dayDifference = dayDiff(baseXword.date, date);
        var weekDifference = dayDifference % 7;
        var xWordNo = baseXword.no + weekDifference;
        return xWordNo;
    }
}

function prizeForDate(date) {
    return getNoForWeeklyXword(date, baseSaturdayPrize, 6)
}

function speedyForDate(date) {
    return getNoForWeeklyXword(date, baseSundaySpeedy, 6)
}

function quipticForDate(date) {
    return getNoForWeeklyXword(date, baseMondayQuiptic, 6)
}
