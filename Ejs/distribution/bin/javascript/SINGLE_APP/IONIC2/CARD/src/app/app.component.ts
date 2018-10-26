import { Component } from '@angular/core';
import { Platform } from 'ionic-angular';
import { StatusBar } from '@ionic-native/status-bar';
import { SplashScreen } from '@ionic-native/splash-screen';
import { Keyboard } from '@ionic-native/keyboard';
import { ScreenOrientation } from '@ionic-native/screen-orientation';

import { CardPage } from '../pages/card/card';

declare var app_locking: any;

@Component({
  templateUrl: 'app.html',
  providers: [Keyboard,ScreenOrientation]
})

export class MyApp {
  rootPage:any = CardPage;

  constructor(platform: Platform, statusBar: StatusBar, splashScreen: SplashScreen, keyboard: Keyboard, screenOrientation: ScreenOrientation) {
    platform.ready().then(() => {
      statusBar.styleDefault();
      splashScreen.hide();
	  
	  keyboard.hideKeyboardAccessoryBar(false);
	  keyboard.disableScroll(true);

	  // locking screen orientation
	  switch(app_locking) { 
		case 0: // portrait
			screenOrientation.lock("portrait").catch(function() {});
			break;
		case 1: // landscape
			screenOrientation.lock("landscape").catch(function() {});
			break;
		default: // nothing
	  }
    });	
  }
}
