import { Component, ViewChild } from '@angular/core';
import { Tabs } from 'ionic-angular';

import { HomePage } from '../home/home';
import { AboutPage } from '../about/about';

declare var app_toc: any;
declare var app_simulation_first: any;
declare var app_simulation_index: any;

@Component({
  templateUrl: 'tabs.html'
})

export class TabsPage {
  @ViewChild('myTabs') tabRef: Tabs;

  tabs: any;
  app_simulation_first: boolean;
  app_simulation_index: number;

  constructor() {
	// app vars
	this.tabs = [];
	for(var i=0; i<app_toc.length; i++) {
		if(app_toc[i].type == "other_page") {
			this.tabs.push({title: "About", icon: "information-circle", root: AboutPage, myref: {index: i}});
		} else if(i == app_simulation_index) {
			this.tabs.push({title: "Simulation", icon: "play", root: HomePage, myref: {index: i}});
		} else {
			this.tabs.push({title: String(app_toc[i].title), icon: "document", root: HomePage, myref: {index: i}});			
		}
	}

	this.app_simulation_first = app_simulation_first;
	this.app_simulation_index = app_simulation_index;
  }

  ionViewDidEnter() {
	if(this.app_simulation_first) 
		this.tabRef.select(app_simulation_index);
	else
		this.tabRef.select(0);
  }   
}

