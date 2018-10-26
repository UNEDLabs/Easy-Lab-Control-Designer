import { ElementRef, Component, ViewChild } from '@angular/core';
import { Content, NavController, NavParams } from 'ionic-angular';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

declare var app_toc: any;
declare var app_full_screen: any;

@Component({
  selector: 'page-home',
  templateUrl: 'home.html'
})

export class HomePage {
  @ViewChild(Content) content: Content;

  app_full_screen: boolean;
  hideNavigation: boolean;
  
  currentPage: any;
  pages: any;
  url: SafeUrl;

  constructor(public navCtrl: NavController, private sanitizer: DomSanitizer, public myElement: ElementRef, public navParams: NavParams) {
	// app vars
	this.app_full_screen = app_full_screen;
	this.pages = app_toc;
	
	// navigation vars
    this.currentPage = this.pages[navParams.get("index")] || this.pages[0];
	this.url = sanitizer.bypassSecurityTrustResourceUrl(this.currentPage.url);
	
	// full screen
    this.hideNavigation = app_full_screen; 	// fullScreen requires hideNavigation
  }	

  resize(event) {
	// needed to resize iframe into template (otherwise header over iframe)
	this.content.resize();
  }
  
}

