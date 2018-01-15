//redFrik

//--related:
//RedMixerChannelGUI RedEffectsRackGUI RedMatrixMixerGUI

//--todo:
//differentiate mixers and channels more graphically
//fix the rest of the presets
//textviews at the bottom for temporary notes
//multiple channels (at least mono/stero)
//preview/monitor synth and connect to numbox

RedMixerGUI {
	classvar <>numMixerChannelsBeforeScroll= 11;	//todo!!!
	var <win, <redMixer, <time, lastTime= 0, controllers,
	mainGUIchannels, mainGUImixers, mainGUIviews, mirrorGUIviews;
	*new {|redMixer, position|
		^super.new.initRedMixerGUI(redMixer, position);
	}
	initRedMixerGUI {|argRedMixer, position|
		Routine({
			var tab, winWidth, winHeight, tabWidth, tabHeight, topHeight,
			macroMenu, macroFunctions, makeMirror, defaults,
			margin= Point(4, 4), gap= Point(4, 4), lagBox;
			while({argRedMixer.isReady.not}, {0.02.wait});
			controllers= List.new;

			redMixer= argRedMixer;
			position= position ?? {Point(300, 200)};

			tabWidth= RedMixerChannelGUI.width+gap.x;
			tabWidth= tabWidth*(redMixer.mixers.size+redMixer.channels.size);
			topHeight= 10;
			tabHeight= RedMixerChannelGUI.height+2+topHeight;

			winWidth= (tabWidth+(margin.x*2)).max(260);
			winHeight= tabHeight+topHeight+gap.y+(margin.y*2)+16;
			win= Window(
				redMixer.class.name.asString.put(0, $r),
				Rect(position.x, position.y, winWidth, winHeight),
				false
			);
			win.front;
			win.view.background= GUI.skins.redFrik.background;
			win.view.decorator= FlowLayout(win.view.bounds, margin, gap);

			lagBox= RedNumberBox(win)
			.value_(redMixer.lag)
			.action_({|v|
				redMixer.lag= v.value.max(0);
			});
			controllers.add(
				SimpleController(redMixer.cvs.lag).put(\value, {|ref|
					lagBox.value= ref.value;
				})
			);
			win.view.decorator.shift(-2, 2);
			RedStaticText(win, nil, "lag");
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
					{gui.do{|x| x.do{|y| y.view.valueAction= 1.0.rand}}},
					{gui.do{|x| x.do{|y| if(0.3.coin, {y.view.valueAction= 1.0.rand})}}},
					{gui.do{|x| x.do{|y| y.view.valueAction= y.view.value+0.08.rand2}}},
					{gui.do{|x| x.do{|y| if(0.3.coin, {y.view.valueAction= y.view.value+0.08.rand2})}}},
					//{gui.do{|x| x.valueEq= {|y, cv| cv.spec.unmap(cv.spec.default)}}},
					//{gui.do{|x| x.valueEq= {1.0.rand}}},
					//{gui.do{|x| x.valueEq= {|y| if(0.3.coin, {1.0.rand}, {y.value})}}},
					//{gui.do{|x| x.valueEq= {|y| y.value+0.08.rand2}}},
					//{gui.do{|x| x.valueEq= {|y| if(0.3.coin, {y.value+0.08.rand2}, {y.value})}}},
					{mainGUIviews.do{|x, i| x.do{|y, j| y.view.valueAction= mirrorGUIviews[i][j].view.value}}},
					{mirrorGUIviews.do{|x, i| x.do{|y, j| y.view.valueAction= mainGUIviews[i][j].view.value}}},
					{Dialog.savePanel{|x| redMixer.store(x)}},
					{Dialog.openPanel{|x|
						redMixer.free;
						redMixer= RedMixer.restoreFile(x);
						redMixer.init;
						redMixer.gui(Point(win.bounds.left, win.bounds.top));
						this.close;
					}}
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
				/*"eq: defaults",
				"eq: randomize all",
				"eq: randomize some",
				"eq: vary all",
				"eq: vary some",*/
				"copy <",
				"copy >",
				"save preset",
				"load preset"
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
				mainGUIchannels= redMixer.channels.collect{|x, i|
					RedMixerChannelGUI(x, v, nil, "in[%,%]".format(x.out, x.out+1));
				};
				mainGUImixers= redMixer.mixers.collect{|x, i|
					RedMixerChannelGUI(x, v, nil, "out[%,%]".format(x.out, x.out+1));
				};
			};
			mainGUIviews= (mainGUIchannels++mainGUImixers).collect{|x| x.views};
			defaults= mainGUIviews.collect{|x| x.collect{|y| y.view.value}};
			makeMirror= {|v, x|
				var views= List.new;
				var cmp= CompositeView(v, x.cmp.bounds);
				var ampRef= x.redMixerChannel.cvs.amp;	//special case to keep this link
				var ampRefCopy= ampRef.copy;
				x.views.do{|y|
					var r;
					if(y.ref==ampRef, {
						r= ampRefCopy;
					}, {
						r= y.ref.copy;
					});
					views.add(y.class.new(cmp, y.view.bounds, r, y.map, y.unmap, y.args));
				};
				views;
			};
			tab.views[1].flow{|v|
				mirrorGUIviews= (mainGUIchannels++mainGUImixers).collect{|x|
					makeMirror.value(v, x);
				};
			};
			redMixer.channels.do{|x, i|
				controllers.add(
					SimpleController(x.cvs.out).put(\value, {|ref|
						mainGUIchannels[i].text_("in[%,%]".format(ref.value, ref.value+1));
					})
				);
			};
			redMixer.mixers.do{|x, i|
				controllers.add(
					SimpleController(x.cvs.out).put(\value, {|ref|
						mainGUImixers[i].text_("out[%,%]".format(ref.value, ref.value+1));
					})
				);
			};

			win.onClose_({controllers.do{|x| x.remove}});
			CmdPeriod.doOnce({this.close});
		}).play(AppClock);
	}
	close {
		if(win.isClosed.not, {win.close});
		controllers.do{|x| x.remove};
	}
}
