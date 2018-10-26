angular.module('starter', ['ionic'])

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

.config(function($stateProvider, $urlRouterProvider) {
  $stateProvider

  // Each tab has its own nav history stack:
  .state('description', {
    url: '/description',
    templateUrl: 'slides-description.html',
    controller: 'DescriptionCtrl'
  })

  .state('simulation', {
      url: '/simulation',
      templateUrl: 'simulation.html',
      controller: 'SimulationCtrl'
  });
 
  // if none of the above states are matched, use this as the fallback
  $urlRouterProvider.otherwise('/description');

})

.controller('DescriptionCtrl', function($scope, $state) {
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
    
    $scope.isIOS = ionic.Platform.isIOS();
    $scope.isAndroid = ionic.Platform.isAndroid();
	
	// slider options
	$scope.sliderOptions = {
		effect: 'slide',
		paginationHide: false,
		initialSlide: 0,
		onInit: function(swiper){
			$scope.swiper = swiper;
		},
		onSlideChangeEnd: function(swiper){
			// ....
		},
		slides: []
	};
	
	for(var i=0; i<app_simulation_index; i++) {
		$scope.sliderOptions.slides.push({'template':app_toc[i].url})
	}
	$scope.sliderOptions.slides.push({'template':'other_pages/about.html'})
	
	$scope.skipDescription = function() {
		console.log("skip");
		$state.go("simulation");
	}
})

.controller('SimulationCtrl', function($scope) {
	$scope.currentPage = app_toc[app_simulation_index];	
	$scope.$hasHeader = false;
})

.controller('AppReaderCtrl', function($scope,$state) {
	$scope.title = app_title;	
});

