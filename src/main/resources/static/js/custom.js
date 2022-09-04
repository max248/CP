/*------------------------------------------------------------------
    File Name: custom.js
    Template Name: Pluto - Responsive HTML5 Template
    Created By: html.design
    Envato Profile: https://themeforest.net/user/htmldotdesign
    Website: https://html.design
    Version: 1.0
-------------------------------------------------------------------*/
"use strict";

$(document).ready(function () {
  /*-- sidebar js --*/
  $('#sidebarCollapse').on('click', function () {
    $('#sidebar').toggleClass('active');
  });
  /*-- tooltip js --*/
  // $('[data-toggle="tooltip"]').tooltip();
});

// var ps = new PerfectScrollbar('#sidebar');


function switcher(){
  $("body").toggleClass("bg-secondary");
  $(".section,.rd-navbar-main-outer, .container, .row").toggleClass("dark-mode");
  $("nav").toggleClass("nav-dark-mode");
  $(".custom-control-label").toggleClass("text-white");
  $("h1, h2,h6,h3, p, div,.rd-nav-item,.rd-nav-link,.lang").toggleClass("text-white");
}

function appendItem(response){
  var rowTxt = ''
  for (var i=0;i<response.length; i++){
    var colDiv = '';
    var tagDiv = 'Tags: ';
    if(response[i].json[0].json.columns != null)
      response[i].json[0].json.columns.forEach(function(col){
        colDiv +=
            col.name + ': <span>' + col.data + '</span><br>';
      });
    if(response[i].json[0].json.tags != null)
      response[i].json[0].json.tags.forEach(function(tag){
        tagDiv += '<a href="#"> #<span style="color: #00aced">' + tag.name + '</span> </a>';
      });
    var starTxt = '';
    for(var j=1;j<=5;j++){
      var txt = response[i].json[0].overall_rate >= j ? '<i class="fa fa-star"></i>' : '<i class="fa fa-star-o"></i>';
      starTxt +=
          '<li class="list-inline-item"><a href="#" onclick="addRate(' + response[i].json[0].id + ',' + j + ')"> ' + txt + '</a></li>';
    }
    rowTxt +=
        '<div class="testimonial-box">' +
        '<div class="testimonial-title">' + response[i].json[0].name + '</div>' +
        '<div>' +
        '<img src="' + response[i].json[0].image_url + '" ' +
        'alt=""/>' +
        '</div>' +
        '<div class="testimonial-text">' +
        '<p class="item-price">' + colDiv + tagDiv +
        '<br>' + txtComments + ' :<span style="color: #4de17c"> ' +
        '<a href="/item?item_id=' + response[i].json[0].id + '">' + response[i].json[0].comment_count + '</a></span></p>' +
        '</div>' +
        '<div class="testimonial-name">' + txtCollection +
        ' : <a href="/collection?collection_id=' + response[i].json[0].collection_id + '">' + response[i].json[0].collection_name + '</a></div>' +
        '<div class="star-rating">' +
        '<ul class="list-inline">' + starTxt +
        '</ul>' +
        '</div>' +
        '<a href="/item?item_id=' + response[i].json[0].id + '" class="btn btn-primary">' + txtDetails + '</a>'+
        '</div>';
  }
  var divTxt = '<div class="owl-carousel owl-theme-1" data-items="1" data-sm-items="1" data-md-items="1" data-lg-items="1" data-xl-items="2" data-xxl-items="3" data-margin="15px" data-nav="false" data-dots="true">';
  divTxt += rowTxt + '</div>';
  $('#myItemCarousel').append(divTxt);
}

function addRate(itemId,rate){
  $.ajax({
    url: "/add_rate",
    type: "POST",
    data: 'item_id=' + itemId + "&rate=" + rate,
    success: function (response){
      if(response == 'success'){
        window.location.reload();
      } else if(response == 'login'){
        window.location.href = "/login";
      }
    },
    error: function (error){
      console.log("error: " + error)
    }
  });
}
