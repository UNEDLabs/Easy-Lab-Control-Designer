// Ionic BookReader App

// angular.module is a global place for creating, registering and retrieving Angular modules
// 'starter' is the name of this angular module example (also set in a <body> attribute in index.html)
// the 2nd parameter is an array of 'requires'

var selectRefs = function (index) {
}

var myModule = angular.module('BookReader', ['ionic', 'ui.router', 'ngSanitize'])

.run(function($ionicPlatform) {
  $ionicPlatform.ready(function() {
    if(window.cordova && window.cordova.plugins.Keyboard) {
      // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
      // for form inputs)
      cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);

      // Don't remove this line unless you know what you are doing. It stops the viewport
      // from snapping when text inputs are focused. Ionic handles this internally for
      // a much nicer keyboard experience.
      cordova.plugins.Keyboard.disableScroll(true);
    }
    if(window.StatusBar) {
      StatusBar.styleDefault();
    }
  });
})

.factory('Sections', function() {
  return {
    selectLastItem: function($scope) {
      var index = window.localStorage['lastActiveSection'];
      if (typeof index == "undefined") {
        $scope.selectPage($scope.TOC);
        return;
      }
      var intIndex = parseInt(index);
      if (intIndex>=$scope.sections.length) intIndex =  $scope.TOC;
      //console.log ("index = "+index+" intIndex = "+intIndex);
      $scope.selectPage(intIndex);
    },
    saveLastItem: function(index) {
      window.localStorage['lastActiveSection'] = index;
    }
  };
})

.config(function($stateProvider, $urlRouterProvider) {
  $stateProvider
  .state('cover', {
    url: '/cover',
    templateUrl: 'other_pages/cover.html'
  })
  .state('title-page', {
    url: '/title-page',
    templateUrl: 'other_pages/title_page.html'
  })
  .state('about', {
    url: '/',
    templateUrl: 'other_pages/about.html'
  })
  .state('toc', {
    url: '/toc',
    templateUrl: 'toc.html'
  })
  .state('section', {
    url: '/section',
    templateUrl: 'section.html'
  })
  .state('references', {
    url: '/references',
//    controller: 'indexController'
    templateUrl: 'other_pages/references.html'
  })
  .state('copyright', {
    url: '/copyright',
    templateUrl: 'other_pages/copyright.html'
  });
  $urlRouterProvider.otherwise("/toc");
})

.controller('BookReaderCtrl', function($scope, $state, Sections, $ionicSideMenuDelegate,$ionicHistory, $window,
    $location, $anchorScroll, $ionicScrollDelegate, $timeout) {
  $scope.book_title = book_title;
  $scope.book_author = book_author;
  $scope.book_menu_title = book_menu_title;

  $scope.contents = book_toc;
  $scope.index = -1;
  $scope.currentSection = null;
  $scope.sections = [];

  // Special pages
  $scope.COVER = -1;
  $scope.TITLE_PAGE = -2;
  $scope.TOC   = -3;
  $scope.ABOUT = -4;
  $scope.REFERENCES = -5;
  $scope.COPYRIGHT = -6;

  $scope.isIOS = ionic.Platform.isIOS();
  $scope.isAndroid = ionic.Platform.isAndroid();

  var otherPages = [
    { url:'cover',      title: book_cover_title      },
    { url:'title-page', title: book_title_page_title },
    { url:'toc',        title: book_toc_title        },
    { url:'about',      title: book_about_title      },
    { url:'references', title: book_references_title },
    { url:'copyright',  title: book_copyright_title  }
  ];

  $scope.getOtherPage = function(index) {
    return otherPages[-index-1];
  };

  function initSections() {
    var counter = 0;
    for (var i=0, n=$scope.contents.length; i<n; i++) {
      var entry = $scope.contents[i];
      if (entry.type=="chapter") {
        for (var j=0, m=entry.sections.length; j<m; j++) {
          $scope.sections[counter++] = entry.sections[j];
        }
      }
      else $scope.sections[counter++] = entry;
    }
  };

  function indexOfSection(section) {
    for (var i=0, n=$scope.sections.length; i<n; i++) {
      if ($scope.sections[i]==section) return i;
    }
    return $scope.TOC;
  };

  initSections();

  $scope.calculateDimensions = function(gesture) {
    $scope.dev_width = $window.innerWidth;
    $scope.dev_height = $window.innerHeight;
  };

  angular.element($window).bind('resize', function(){
    $scope.$apply(function() {
      $scope.calculateDimensions();
      $scope.setTitle($scope.index);
    });
  });

  $scope.calculateDimensions();

  function setItem(index, anchor) {
    $scope.index = index;
    $ionicHistory.nextViewOptions({
                  disableAnimate: true,
                  disableBack: true
                });
    var url;
    if (index<0) url = $scope.getOtherPage(index).url;
    else url = 'section';
    console.log ("URL = "+url);
    $state.go(url);
    if (anchor) {
      console.log ("Anchor = "+anchor);
      $timeout(function() {
        $location.hash(anchor);
        $ionicScrollDelegate.$getByHandle('ref_list');
        //handle.anchorScroll();
        //handle.scrollTop();
      }, 100);
    }
    //$ionicSideMenuDelegate.toggleLeft(false);
    Sections.saveLastItem(index);
    $scope.setTitle(index);
  };

  $scope.setTitle = function(index) {
    var itemTitle;
    if (index<0) itemTitle = $scope.getOtherPage(index).title;
    else itemTitle = $scope.sections[index].title;
//    if ($scope.dev_width>800) $scope.header_title = book_title+" - "+itemTitle;
//    else $scope.header_title = itemTitle;
    if ($scope.dev_width>800) $scope.book_title = book_title+" - ";
    else $scope.book_title = "";
    $scope.header_title = itemTitle;
  };

  $scope.toggleTOC = function(){
    $ionicSideMenuDelegate.toggleLeft();
  };

  $scope.selectPage = function(index, anchor){
    //console.log("Select page "+index);
    setItem(index,anchor);
    if (index<0) {
      $scope.hidePrevious = true;
      $scope.hideNext = true;
    }
    else {
      $scope.hidePrevious = index<=0;
      $scope.hideNext = index>=$scope.sections.length-1;
      $scope.currentSection = $scope.sections[index];
    }
  };

  selectRefs = function (index) {
    $scope.selectPage($scope.REFERENCES,index);
  };

  $scope.selectSection = function(section) {
    var index = indexOfSection(section);
    $scope.selectPage( index<0 ? $scope.TOC : index);
  };

  $scope.nextSection = function() {
    if ($scope.index<$scope.sections.length-1) $scope.selectPage($scope.index+1);
  };

  $scope.previousSection = function() {
    if ($scope.index>0) $scope.selectPage($scope.index-1);
  };

  $scope.toggleChapter = function(chapter) {
    if (chapter.expanded) chapter.expanded = false;
    else chapter.expanded = true;
  };

  $scope.isChapterExpanded = function(chapter) {
    return chapter.expanded;
  };

  $scope.myGoBack = function() {
    console.log("Should go back");
    $ionicHistory.goBack();
  };

  Sections.selectLastItem($scope);

});
