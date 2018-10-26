import { Component, ViewChild } from '@angular/core';
import { Nav, Platform } from 'ionic-angular';
import { StatusBar } from '@ionic-native/status-bar';
import { SplashScreen } from '@ionic-native/splash-screen';
import { Keyboard } from '@ionic-native/keyboard';
import { ScreenOrientation } from '@ionic-native/screen-orientation';

import { HomePage } from '../pages/home/home';
import { AboutPage } from '../pages/about/about';

declare var app_locking: any;
declare var app_toc: any;
declare var app_menu_title: any;
declare var app_simulation_first: any;
declare var app_simulation_index: any;

@Component({
  templateUrl: 'app.html',
  providers: [Keyboard,ScreenOrientation]
})

export class MyApp {
  @ViewChild(Nav) nav: Nav;

  pages: any;
  app_menu_title: string;
  app_simulation_first: boolean;
  app_simulation_index: number;

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

	// app vars
	this.pages = app_toc;
	this.app_menu_title = app_menu_title;
	this.app_simulation_first = app_simulation_first;
	this.app_simulation_index = app_simulation_index;
  }

  ngOnInit() {
	if(this.app_simulation_first) 
		this.nav.setRoot(HomePage, { currentPage: this.pages[app_simulation_index] });		
	else
		this.nav.setRoot(HomePage, { currentPage: this.pages[0] });			
  }
   
  selectPage(page) {
	if(page.type == "other_page")
		this.nav.setRoot(AboutPage);
	else
		this.nav.setRoot(HomePage, { currentPage: page });	
  }
}


