//redFrik

//time should also be a cv or?
//lag box?
//window height fix

//--related:
//RedEffectsRack RedEffectModule RedEffectModuleGUI RedMixerGUI

RedEffectsRackGUI {
	classvar <>numEffectsBeforeScroll= 9;
	var <win, <redEffectsRack, <time, lastTime= 0, controllers,
	mainGUI, mainGUIviews, mirrorGUIviews,
	width1= 264, height1;
	*new {|redEffectsRack, position|
		^super.new.initRedEffectsRackGUI(redEffectsRack, position);
	}
	initRedEffectsRackGUI {|argRedEffectsRack, position|
		Routine({
			var tab, winWidth, winHeight, tabWidth, tabHeight, topHeight,
			macroMenu, macroFunctions, makeMirror, defaults,
			margin= Point(4, 4), gap= Point(4, 4), outBox, rows;
			while({argRedEffectsRack.isReady.not}, {0.02.wait});
			controllers= List.new;

			redEffectsRack= argRedEffectsRack;
			position= position ?? {Point(300, 360)};

			redEffectsRack.efxs.do{|x|
				var cols= x.def.metadata[\order].count{|x| x.key!=\out}-1;
				var tmp= gap.x*2.5+RedGUICVSlider.defaultWidth+gap.x;
				tmp= tmp+(RedGUICVKnob.defaultWidth+gap.x*cols);
				if(tmp>width1, {width1= tmp});
			};
			height1= gap.y+RedGUICVSlider.defaultHeight+gap.y;

			rows= redEffectsRack.efxs.size.min(numEffectsBeforeScroll);
			tabWidth= width1;
			topHeight= 14;
			tabHeight= height1+gap.y*rows+topHeight;

			winWidth= tabWidth+(margin.x*2);
			winHeight= tabHeight+topHeight+gap.y+(margin.y*2)+12;
			win= Window(
				redEffectsRack.class.name.asString.put(0, $r),
				Rect(position.x, position.y, winWidth, winHeight),
				false
			);
			win.front;
			win.view.background= GUI.skins.redFrik.background;
			win.view.decorator= FlowLayout(win.view.bounds, margin, gap);

			outBox= RedNumberBox(win)
			.value_(redEffectsRack.out)
			.action_({|v|
				redEffectsRack.out= v.value;
			});
			controllers.add(
				SimpleController(redEffectsRack.cvs.out).put(\value, {|ref|
					outBox.value= ref.value;
				})
			);
			win.view.decorator.shift(-2, 2);
			RedStaticText(win, nil, "out");
			win.view.decorator.shift(2, -2);
			RedButton(win, nil, "monitor", "monitor").action_{|view| "todo".postln};  //TODO
			RedNumberBox(win).value_(7).action_{|view| "todo".postln};

			macroFunctions= {|index|
				var gui;
				if(tab.activeTab==0, {
					gui= mainGUIviews;
				}, {
					gui= mirrorGUIviews;
				});
				[
					{},
					{gui.do{|x, i| x.do{|y, j| y.view.valueAction= defaults[i][j]}}},
					{gui.do{|x| x.do{|y, j| if(j==0 or:{j%2==1}, {y.view.valueAction= 1.0.rand})}}},
					{gui.do{|x| x.do{|y, j| if(j==0 or:{j%2==1}, {if(0.3.coin, {y.view.valueAction= 1.0.rand})})}}},
					{gui.do{|x| x.do{|y, j| if(j==0 or:{j%2==1}, {y.view.valueAction= y.view.value+0.08.rand2})}}},
					{gui.do{|x| x.do{|y, j| if(j==0 or:{j%2==1}, {if(0.3.coin, {y.view.valueAction= y.view.value+0.08.rand2})})}}},
					{gui.collect{|x| x.select{|y, j| j==0 or:{j%2==1}}.choose}.choose.view.valueAction= #[0, 0.5, 1].choose},
					{gui.do{|x| x.do{|y, j| if(j==0 or:{j%2==1}, {y.view.valueAction= 0})}}},
					{mainGUIviews.do{|x, i| x.do{|y, j| y.view.valueAction= mirrorGUIviews[i][j].view.value}}},
					{mirrorGUIviews.do{|x, i| x.do{|y, j| y.view.valueAction= mainGUIviews[i][j].view.value}}},
					{Dialog.savePanel{|x| redEffectsRack.cvs.postln.writeArchive(x)}},
					/*{Dialog.openPanel{|x|
						redMixer.free;
						redMixer= RedMixer.restoreFile(x);
						redMixer.init;
						redMixer.gui(Point(win.bounds.left, win.bounds.top));
						this.close;
					}}*/
					{Dialog.openPanel{|x| Object.readArchive(x).keysValuesDo{|k, v|
						redEffectsRack.cvs[k].value_(v.value).changed(\value);
					}}}
				][index].value;
			};
			macroMenu= RedPopUpMenu(win)
			.items_([
				"_macros_",
				"defaults",
				"randomize all",
				"randomize some",
				"vary all",
				"vary some",
				"surprise",
				"clear",
				"copy <",
				"copy >",
				"save preset",
				"load preset",
				"#1", "#2", "#3", "#4", "#5"
			])
			.action_{|view|
				macroFunctions.value(view.value);
			};
			RedButton(win, Point(14, 14), "<").action= {
				macroFunctions.value(macroMenu.value);
			};
			win.view.decorator.nextLine;

			win.view.decorator.shift(100, 0);
			time= RedSlider(win)
			.action_{|view|
				if(tab.activeTab==0, {
					if(lastTime==0 and:{view.value>0}, {
						mainGUIviews.do{|x| x.do{|y| y.save}};
					});
					mainGUIviews.do{|x, i|
						x.do{|y, j|
							y.interp(mirrorGUIviews[i][j].value, view.value);
						};
					};
					tab.backgrounds_([
						GUI.skins.redFrik.foreground.copy.alpha_(1-view.value),
						GUI.skins.redFrik.background
					]);
				}, {
					view.value= 1;
				});
				lastTime= view.value;
			};
			win.view.decorator.shift(-100, -4);

			tab= TabbedView(
				win,
				Point(tabWidth, tabHeight),
				#[\now, \later],
				[Color.grey(0.2, 0.2), GUI.skins.redFrik.background],
				scroll: true
			);
			tab.views.do{|x| x.hasHorizontalScroller= false};
			if(redEffectsRack.efxs.size<=numEffectsBeforeScroll, {
				tab.views.do{|x| x.hasVerticalScroller= false};
			});
			tab.font= RedFont.new;
			tab.stringFocusedColor= GUI.skins.redFrik.foreground;
			tab.stringColor= GUI.skins.redFrik.foreground;
			tab.backgrounds= [GUI.skins.redFrik.foreground, GUI.skins.redFrik.background];
			tab.unfocusedColors= [Color.grey(0.2, 0.2), GUI.skins.redFrik.background];
			tab.focusActions= [
				{
					time.valueAction= lastTime;
					time.enabled= true;
					time.canFocus= true;
				},
				{
					mainGUIviews.do{|x| x.do{|y| y.save}};
					time.value= 1;
					time.enabled= false;
					time.canFocus= false;
				}
			];
			tab.views[0].flow{|v|
				mainGUI= redEffectsRack.efxs.collect{|x|
					var gui= RedEffectModuleGUI(x, v);
					v.view.decorator.nextLine;
					gui;
				};
			};
			mainGUIviews= mainGUI.collect{|x| x.views};
			//mainGUIviews= mainGUI.select{|x, i| i==0 or:{i%2==1}}.collect{|x| x.views};
			defaults= mainGUIviews.collect{|x| x.collect{|y| y.view.value}};
			makeMirror= {|v, x|
				var views= List.new;
				var cmp= CompositeView(v, x.cmp.bounds);
				var m= x.views[0];	//first the single mixer slider
				views.add(m.class.new(cmp, m.view.bounds, m.ref.copy, m.map, m.unmap, m.args));
				x.views.copyToEnd(1).pairsDo{|y, z, j|
					var p= x.params[j.div(2)+1].value;
					var r= x.redEffectModule.cvs[p].copy;	//then the knobs and numbers
					views.add(y.class.new(cmp, y.view.bounds, r, y.map, y.unmap, y.args));
					views.add(z.class.new(cmp, z.view.bounds, r, z.map, z.unmap, z.args));
				};
				views;
			};
			tab.views[1].flow{|v|
				mirrorGUIviews= mainGUI.collect{|x|
					var gui= makeMirror.value(v, x);
					v.view.decorator.nextLine;
					gui;
				};
			};

			CmdPeriod.doOnce({if(win.isClosed.not, {win.close})});
		}).play(AppClock);
	}
	close {
		if(win.isClosed.not, {win.close});
		controllers.do{|x| x.remove};
	}
}
