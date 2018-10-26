import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';
import { DomSanitizer } from '@angular/platform-browser';

import { HomePage } from '../home/home';
import { AboutPage } from '../about/about';

declare var app_title: any;
declare var app_toc: any;
declare var app_simulation_index: any;
declare var about_logoImage: any;
declare var app_full_screen: any;

@Component({
  selector: 'page-card',
  templateUrl: 'card.html'
})

export class CardPage {
  title: any;
  pages: any;
  about_logoImage: any;
  hideNavigation: boolean;
  
  constructor(private sanitizer: DomSanitizer, public navCtrl: NavController) {
	// app vars
	this.title = app_title;
	this.about_logoImage = about_logoImage;
	
	this.pages = [];
	for(var i=0; i<app_toc.length; i++) {
		if(app_toc[i].type == "other_page") {
			this.pages.push({title: "About", icon: "information-circle", root: AboutPage, myref: {index: i}});
		} else if(i == app_simulation_index) {
			this.pages.push({title: "Simulation", icon: "play", root: HomePage, myref: {index: i}});
		} else {
			this.pages.push({title: String(app_toc[i].title), icon: "document", root: HomePage, myref: {index: i}});			
		}
	}
	
	this.hideNavigation = app_full_screen;
  }

  itemSelected(item) {
	if(app_toc[item.index].type == "other_page")	
		this.navCtrl.push(AboutPage);
	else
		this.navCtrl.push(HomePage, item);
  }
  
}

