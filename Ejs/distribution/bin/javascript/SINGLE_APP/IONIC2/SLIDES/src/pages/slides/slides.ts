import { Component } from '@angular/core';
import { App } from 'ionic-angular';
import { DomSanitizer } from '@angular/platform-browser';

import { HomePage } from '../home/home';
import { AboutPage } from '../about/about';

declare var app_title: any;
declare var app_toc: any;
declare var app_simulation_index: any;

@Component({
  selector: 'page-slides',
  templateUrl: 'slides.html'
})

export class SlidesPage {
  title: any;
  slides: any;
  AboutRoot:any = AboutPage;
  
  constructor(private sanitizer: DomSanitizer, private app:App) {
	// app vars
	this.title = app_title;
	this.slides = [];
	for(var i=0; i<app_toc.length; i++) {
		if(app_toc[i].type != "other_page" && i != app_simulation_index) {
			this.slides.push({title: app_toc[i].title, url: sanitizer.bypassSecurityTrustResourceUrl(app_toc[i].url)});			
		}
	}
  }
  
  skipDescription(event) {
    this.app.getRootNav().setRoot(HomePage);
  }
  
}

