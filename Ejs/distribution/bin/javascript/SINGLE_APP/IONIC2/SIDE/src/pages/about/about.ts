import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';
import { Platform } from 'ionic-angular';

declare var about_about: any;
declare var about_info: any;
declare var about_logoImage: any;
declare var about_abstractTxt: any;
declare var about_copyright: any;
declare var about_copyrightTxt: any;
declare var about_authorInfo: any;

declare var app_title: any;

@Component({
  selector: 'page-about',
  templateUrl: 'about.html'
})
export class AboutPage {

  app_title: string;
  about_about: string;
  about_info: string;
  about_logoImage: string;
  about_abstractTxt: string;
  about_copyright: string;
  about_copyrightTxt: string;
  about_authorInfo: string;

  isAndroid: boolean;
  isIOS: boolean;

  constructor(public navCtrl: NavController, public plt: Platform) {

	this.app_title = app_title;
	this.about_about = about_about;
	this.about_info = about_info;
	this.about_logoImage = about_logoImage;
	this.about_abstractTxt = about_abstractTxt;
	this.about_copyright = about_copyright;
	this.about_copyrightTxt = about_copyrightTxt;
	this.about_authorInfo = about_authorInfo;

	this.isAndroid = this.plt.is('ios');
	this.isIOS = this.plt.is('android');

  }

}
