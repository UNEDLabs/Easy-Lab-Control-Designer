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
  .state('card', {
    url: '/card',
	templateUrl: 'card.html',
	controller: 'CardCtrl'
  })

  .state('description', {
    url: '/description/:id',
	templateUrl: 'description.html',
	controller: 'DescriptionCtrl'
  })

  .state('simulation', {
      url: '/simulation',
	  templateUrl: 'simulation.html',
	  controller: 'SimulationCtrl'
    })

  .state('about', {
    url: '/about',
	templateUrl: 'other_page.html',
	controller: 'AboutCtrl'
  });
  
  // if none of the above states are matched, use this as the fallback
  $urlRouterProvider.otherwise('/card');

})

.controller('DescriptionCtrl', function($scope,$stateParams) {
	var ix = Math.max(0,$stateParams.id);
	$scope.currentPage = app_toc[ix];

	// bar header
	if($scope.app_full_screen) {
		$scope.$hasHeader = false; 
	}						
})

.controller('SimulationCtrl', function($scope) {
	$scope.currentPage = app_toc[app_simulation_index];	

	// bar header
	if($scope.app_full_screen) {
		$scope.$hasHeader = false; 		
	}						
})

.controller('AboutCtrl', function($scope) {
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
		
	$scope.currentPage = app_toc[app_toc.length-1];	
})

.controller('CardCtrl', function($scope,$state) {
	// app vars	  
    $scope.about_logoImage = about_logoImage;
	$scope.title = app_title;

    $scope.pages = app_toc;
	$scope.options = [];
	for(var i=0; i<app_toc.length; i++) {
		if(app_toc[i].type == "other_page") {
			var tab = {title: "About", iconoff: "ion-ios-information-outline", iconon: "ion-ios-information", href: "#/about"};
		} else if(i == app_simulation_index) {
			var tab = {title: "Simulation", iconoff: "ion-ios-play-outline", iconon: "ion-ios-play", href: "#/simulation"};
		} else {
			var tab = {title: app_toc[i].title, iconoff: "ion-document", iconon: "ion-android-document", href: "#/description/"+i};			
		}
		$scope.options.push(tab);
	}
})

.controller('AppReaderCtrl', function($scope,$state) {
	// app vars	  
	$scope.title = app_title;
	$scope.app_full_screen = app_full_screen; // ignored
});

