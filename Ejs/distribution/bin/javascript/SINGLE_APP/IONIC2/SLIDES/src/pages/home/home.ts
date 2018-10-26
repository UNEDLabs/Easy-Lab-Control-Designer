import { ElementRef, Component, ViewChild } from '@angular/core';
import { Content, NavController } from 'ionic-angular';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

declare var app_toc: any;
declare var app_full_screen: any;
declare var app_simulation_index: any;
declare var app_title: any;

@Component({
  selector: 'page-home',
  templateUrl: 'home.html'
})

export class HomePage {
  @ViewChild(Content) content: Content;

  hideNavigation: boolean;  
  url: SafeUrl;
  title: any;

  constructor(public navCtrl: NavController, private sanitizer: DomSanitizer, public myElement: ElementRef) {
    this.title = app_title;
	
	// navigation vars
	this.url = sanitizer.bypassSecurityTrustResourceUrl(app_toc[app_simulation_index].url);
	
	// full screen
    this.hideNavigation = app_full_screen; 	// fullScreen requires hideNavigation
  }	

  resize(event) {
	// needed to resize iframe into template (otherwise header over iframe)
	this.content.resize();
  }
  
}

