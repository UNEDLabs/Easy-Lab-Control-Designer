angular.module('AppReader', ['ionic'])

.run(function($ionicPlatform) {
  $ionicPlatform.ready(function() {
    // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
    // for form inputs)
    if (window.cordova && window.cordova.plugins && window.cordova.plugins.Keyboard) {
      cordova.plugins.Keyboard.hideKeyboardAccessoryBar(false);
      cordova.plugins.Keyboard.disableScroll(true);
    }
    if (window.StatusBar) { // org.apache.cordova.statusbar required
      StatusBar.styleDefault();
    }
    // require cordova plugin add cordova-plugin-screen-orientation
    if (window.screen.orientation && window.screen.orientation.lock) {
	    switch(app_locking) { // $scope is not defined here
	    	case 0: // portrait
	    		window.screen.orientation.lock('portrait');
	    		break;
	    	case 1: // landscape
	    		window.screen.orientation.lock('landscape');
	    		break;
	    	default: // nothing
	    		window.screen.orientation.unlock();
	    }
	}    	
  });
})

.factory('Pages', function() {
  return {
    selectLastItem: function($scope) {
      var index = window.localStorage['lastActivePage'];
      if (typeof index == "undefined") {
        if($scope.simulationFirst) 
        	$scope.setPage($scope.simulationIndex);
        else
        	$scope.setPage(0);
        return;
      }
      var intIndex = parseInt(index);
      if (isNaN(intIndex) || intIndex<0 || intIndex>=$scope.pages.length) intIndex =  0;
      //console.log ("index = "+index+" intIndex = "+intIndex);
      $scope.setPage(intIndex);
    },
    saveLastItem: function(index) {
      window.localStorage['lastActivePage'] = index;
    }
  };
})

.config(function($stateProvider, $urlRouterProvider) {
  $stateProvider
  
  .state('model_page', {
    url: '/model_page',
    templateUrl: 'model_page'
  })
  
  .state('other_page', {
    url: '/other_pages',
    templateUrl: 'other_page'	
  })

  // if none of the above states are matched, use this as the fallback
  $urlRouterProvider.otherwise('/model_page');
})

.controller('AppReaderCtrl', function($scope, $state, Pages,
      $ionicHistory, $ionicSideMenuDelegate, $ionicScrollDelegate, $ionicModal,
      $window, $location, $anchorScroll, $timeout) {
	// about vars
    $scope.about_about = about_about;
    $scope.about_info = about_info;
    $scope.about_logoImage = about_logoImage;
    $scope.about_abstractTxt = about_abstractTxt;
    $scope.about_copyright = about_copyright;
    $scope.about_copyrightTxt = about_copyrightTxt;
    $scope.about_authorInfo = about_authorInfo;
	// app vars	  
    $scope.app_title = app_title;
    $scope.app_menu_title = app_menu_title;
    $scope.pages = app_toc;
    $scope.fullScreen = app_full_screen;
    $scope.simulationFirst = app_simulation_first;
    $scope.simulationIndex = app_simulation_index;
    $scope.locking = app_locking;

	// navigation vars
    $scope.index = -1;
    $scope.currentPage = null;
    $scope.hideNavigation = app_full_screen; 	// fullScreen requires hideNavigation
    
    $scope.isIOS = ionic.Platform.isIOS();
    $scope.isAndroid = ionic.Platform.isAndroid();
	
    $scope.calculateDimensions = function(gesture) {
      $scope.dev_width = $window.innerWidth;
      $scope.dev_height = $window.innerHeight;
    };

    angular.element($window).bind('resize', function(){
      $scope.$apply(function() {
        $scope.calculateDimensions();
        $scope.header_title = $scope.pages[index].title;
      });
    });

    $scope.calculateDimensions();

    function indexOfPage(page) {
      for (var i=0, n=$scope.pages.length; i<n; i++) {
        if ($scope.pages[i]==page) return i;
      }
      return 0;
    };

    $scope.setPage = function(index, anchor) {
      $scope.index = index;
      $ionicHistory.nextViewOptions({
                    disableAnimate: true,
                    disableBack: true
                  });
      $scope.hidePrevious = index<=0;
      $scope.hideNext = (index>=$scope.pages.length-1);

      var page = $scope.pages[index];
      if (page.type == "other_page") {  // no simulation pages
		$state.go("other_page");
	    $scope.hideNavigation = false; 
	    $scope.$hasHeader = true; 		
	  } else // simulation pages
		$state.go("model_page");  
	  $scope.currentPage = $scope.pages[index];
	  
      Pages.saveLastItem(index);
      $scope.header_title = $scope.pages[index].title;
    };

    $scope.selectPage = function(page) {
      $scope.setPage(indexOfPage(page));
    };

    $scope.toggleTOC = function(){
      $ionicSideMenuDelegate.toggleLeft();
      if ($scope.fullScreen) {
      	// hide navigation bar when side-menu is shown
		$scope.hideNavigation = true;
		$scope.$hasHeader = false;
		$timeout(function() {
			$ionicScrollDelegate.$getByHandle('scrollHandle').scrollBottom();
		}, 100);		  
	  }
    };

    $scope.nextPage = function() {
      if ($scope.index<$scope.pages.length-1) $scope.setPage($scope.index+1);
    };

    $scope.previousPage = function() {
      if ($scope.index>0) $scope.setPage($scope.index-1);
    };

	// main idea: there are two scrolling bars, one is internal and belongs to iframe, another is external and belongs to ion-content.
	// when only scrolling on the window top or bottom, the ion-content scroll events trigger, otherwise triggering iframe events.
	// this listener is only for ion-content scroll events (see index.html)
    $scope.scrollEvent = function() {
      if($scope.fullScreen) { // full screen property
	      $scope.scrollamount = $ionicScrollDelegate.$getByHandle('scrollHandle').getScrollPosition().top;
	      var maxScrollableDistanceFromTop = $ionicScrollDelegate.$getByHandle('scrollHandle').getScrollView().__maxScrollTop;
      	  // compare position scroll and maximum scroll
	      if ($scope.scrollamount < maxScrollableDistanceFromTop) { 
	        // show navigation bar when position scroll not is maximum
	        if($scope.hideNavigation == true) {
				$scope.$apply(function() {
					$scope.hideNavigation = false;
					$scope.$hasHeader = true;  // get space for navigation bar					
				});
			}
	      } else {
	        // hide navigation bar when position scroll is maximum
	        if($scope.hideNavigation == false) {
				$scope.$apply(function() {
				   $scope.hideNavigation = true; 
				   $scope.$hasHeader = false;  // release space for navigation bar
				});			  
			}
		  }
	  }
    }; 

    $scope.$hasHeader = !$scope.hideNavigation;   // get/release space for navigation bar
    Pages.selectLastItem($scope);
	if ($scope.fullScreen) {
		// after loading
		$timeout(function() {
			// io-content scroll to bottom in order to hide navigation bar
			$ionicScrollDelegate.$getByHandle('scrollHandle').scrollBottom();
		  
			// listener to iframe scroll events
			var iframe = document.getElementById("_ion-iframe");
			iframe.addEventListener("load", function() {
				var doc = (this.contentWindow || this.contentDocument);
				if (doc.document) doc = doc.document;
				var lastScrollTop = 0;
				doc.addEventListener("scroll", function(){ 
					var st = this.defaultView.pageYOffset;
					if (st > lastScrollTop){ // downscroll
						// io-content scroll to bottom in order to hide navigation bar
						$ionicScrollDelegate.$getByHandle('scrollHandle').scrollBottom();							
					} else { // upscroll code
						if(st == 0) {
							// io-content scroll to top in order to show navigation bar
							$ionicScrollDelegate.$getByHandle('scrollHandle').scrollTop();							
						}
					}
					lastScrollTop = st;
				}, false);				
			}, false);

		}, 100);		
	}
});