//redFrik

//--related:
//RedEffectsRackGUI

//--ideas:
//time should also be a ref too or?
//multisliderview inte continuerligt
//allt i db istället?
//knobs+numberbox eller annat sätt att visa exakta värden?
//preset med mus xy gui där cirklar visar hur mycket varje kanal. slider att styra omfång, max. jmf processing exempel

RedMatrixMixerGUI {
	var <win, <redMatrixMixer, <time, lastTime= 0,
	mainGUI, mirrorGUI;
	*new {|redMatrixMixer, position|
		^super.new.initRedMatrixMixerGUI(redMatrixMixer, position);
	}
	initRedMatrixMixerGUI {|argRedMatrixMixer, position|
		Routine({
			var tab, winWidth, winHeight, tabWidth, tabHeight, topHeight, tabOffset,
			macroMenu, macroFunctions,
			margin= Point(4, 4), gap= Point(4, 4),
			savePosLeft, savePosTop,
			inNumbers, outNumbers,
			inBox, outBox, lagBox, controllers= List.new,
			nIn= argRedMatrixMixer.nIn,
			nOut= argRedMatrixMixer.nOut;
			while({argRedMatrixMixer.isReady.not}, {0.02.wait});

			redMatrixMixer= argRedMatrixMixer;
			position= position ?? {Point(700, 400)};

			tabWidth= RedGUICVMultiSliderView.defaultWidth+gap.x*nOut;
			tabHeight= RedGUICVMultiSliderView.defaultHeight*nIn+14;
			topHeight= 14+gap.y;
			tabOffset= "o99".bounds(RedFont.new).width;
			winWidth= (tabWidth+(margin.x*2)+tabOffset).max(280);
			winHeight= tabHeight+topHeight+(margin.y*2)+28;
			win= Window(
				"redMatrixMixer nIn"++nIn+"nOut"++nOut,
				Rect(position.x, position.y, winWidth, winHeight),
				false
			);
			win.front;
			win.view.background= GUI.skins.redFrik.background;
			win.view.decorator= FlowLayout(win.view.bounds, margin, gap);

			inBox= RedNumberBox(win)
			.value_(redMatrixMixer.in)
			.action_({|v|
				var val= v.value.round.max(0);
				redMatrixMixer.in= val;
				inNumbers.do{|x, i| x.string= "i"++(i+val)};
			});
			controllers.add(
				SimpleController(redMatrixMixer.cvs.in).put(\value, {|ref|
					inBox.value= ref.value;
					inNumbers.do{|x, i| x.string= "i"++(i+ref.value)};
				})
			);
			win.view.decorator.shift(-2, 2);
			RedStaticText(win, nil, "in");
			win.view.decorator.shift(2, -2);

			outBox= RedNumberBox(win)
			.value_(redMatrixMixer.out)
			.action_({|v|
				var val= v.value.round.max(0);
				redMatrixMixer.out= val;
				outNumbers.do{|x, i| x.string= "o"++(i+val)};
			});
			controllers.add(
				SimpleController(redMatrixMixer.cvs.out).put(\value, {|ref|
					outBox.value= ref.value;
					outNumbers.do{|x, i| x.string= "o"++(i+ref.value)};
				})
			);
			win.view.decorator.shift(-2, 2);
			RedStaticText(win, nil, "out");
			win.view.decorator.shift(2, -2);

			lagBox= RedNumberBox(win)
			.value_(redMatrixMixer.lag)
			.action_({|v|
				redMatrixMixer.lag= v.value.max(0);
			});
			controllers.add(
				SimpleController(redMatrixMixer.cvs.lag).put(\value, {|ref|
					lagBox.value= ref.value;
				})
			);
			win.view.decorator.shift(-2, 2);
			RedStaticText(win, nil, "lag");
			win.view.decorator.shift(2, -2);

			macroFunctions= {|index|
				var gui;
				if(tab.activeTab==0, {
					gui= mainGUI;
				}, {
					gui= mirrorGUI;
				});
				[
					{},
					{gui.do{|x, i|
						var arr= 0.dup(nIn);
						if(i<nIn, {arr= arr.put(i, 1)});
						x.value= arr;
					}},
					{gui.do{|x, i|
						var arr= 0.dup(nIn);
						if(i<nIn, {arr= arr.put(nIn-1-i, 1)});
						x.value= arr;
					}},
					{gui.do{|x, i|
						var arr= 0.dup(nIn);
						if(i<nIn, {arr= arr.put(i, 1).put(nIn-i-1, 1)});
						x.value= arr;
					}},
					{gui.do{|x| x.value= x.value.scramble}},
					{gui.do{|x| x.value= 0.dup(nIn)}},
					{var tmp= (0..nIn-1).scramble; gui.do{|x, i|
						var arr= 0.dup(nIn);
						if(i<nIn, {arr= arr.put(tmp[i], 1)});
						x.value= arr;
					}},
					{var tmp= (0..nIn-1).scramble; gui.do{|x, i|
						var arr= 0.dup(nIn);
						if(i<nIn, {arr= arr.put(tmp[i], 1.0.rand)});
						x.value= arr;
					}},
					{gui.do{|x, i| x.value= {0.2.coin.binaryValue}.dup(nIn)}},
					{gui.do{|x, i| x.value= {if(0.2.coin, {1.0.rand}, {0})}.dup(nIn)}},
					{gui.do{|x| x.value= x.value.rotate(-1)}},
					{gui.do{|x| x.value= x.value.rotate(1)}},
					{
						var tmp= gui.collect{|x| x.value};
						gui.do{|x, i| x.value= tmp.wrapAt(i+1)}
					},
					{
						var tmp= gui.collect{|x| x.value};
						gui.do{|x, i| x.value= tmp.wrapAt(i-1)}
					},
					{mainGUI.do{|x, i| x.value= mirrorGUI[i].value}},
					{mirrorGUI.do{|x, i| x.value= mainGUI[i].value}},
					{Dialog.savePanel{|x| redMatrixMixer.cvs.writeArchive(x)}},
					{Dialog.openPanel{|x| Object.readArchive(x).keysValuesDo{|k, v|
						redMatrixMixer.cvs[k].value_(v.value).changed(\value);
						mainGUI.do{|x| x.save};
					}}}
				][index].value;
			};
			macroMenu= RedPopUpMenu(win)
			.items_([
				"_macros_",
				"default",
				"backwards",
				"cross",
				"scramble",
				"clear",
				"urn binary",
				"urn float",
				"random binary",
				"random float",
				"shift up",
				"shift down",
				"shift left",
				"shift right",
				"copy <",
				"copy >",
				"save preset",
				"load preset"
			])
			.action_{|view|
				macroFunctions.value(view.value);
				mainGUI.do{|x| x.save};
			};
			RedButton(win, Point(14, 14), "<").action= {
				macroFunctions.value(macroMenu.value);
				mainGUI.do{|x| x.save};
			};
			win.view.decorator.nextLine;

			win.view.decorator.shift(100+tabOffset, 0);
			time= RedSlider(win)
			.action_{|view|
				if(tab.activeTab==0, {
					if(lastTime==0 and:{view.value>0}, {
						mainGUI.do{|x| x.save};
					});
					mainGUI.do{|x, i|
						x.interp(mirrorGUI[i].value, view.value);
					};
					tab.backgrounds_([
						GUI.skins.redFrik.foreground.copy.alpha_(1-view.value),
						GUI.skins.redFrik.background
					]);
				});
				lastTime= view.value;
			};

			win.view.decorator.shift(-100-tabOffset-time.bounds.width-4, 10);
			savePosLeft= win.view.decorator.left+tabOffset;
			savePosTop= win.view.decorator.top;
			inNumbers= {|i|
				var v= RedStaticText(win, nil, "i"++i);
				var h= RedGUICVMultiSliderView.defaultHeight;
				v.bounds= Rect(
					v.bounds.left,
					savePosTop+14+(i*h)+(h*0.2),
					tabOffset,
					v.bounds.height
				);
				win.view.decorator.nextLine;
				v;
			}.dup(nIn);

			win.view.decorator.left= savePosLeft;
			win.view.decorator.top= savePosTop;

			tab= TabbedView(
				win,
				Point(tabWidth, tabHeight),
				#[\now, \later],
				[Color.grey(0.2, 0.2), GUI.skins.redFrik.background]
			);
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
					mainGUI.do{|x| x.save};
					time.value= 1;
					time.enabled= false;
					time.canFocus= false;
				}
			];
			tab.views[0].flow{|v|
				mainGUI= {|i|
					var ref= redMatrixMixer.cvs[("o"++i).asSymbol];
					var vi= RedGUICVMultiSliderView(v, nil, ref, nil, nil, (num: ref.value.size));
					vi.view.action= vi.view.action+{vi.save};
					vi;
				}.dup(nOut);
			};
			tab.views[1].flow{|v|
				mirrorGUI= {|i|
					var ref= redMatrixMixer.cvs[("o"++i).asSymbol].copy;
					var vi= RedGUICVMultiSliderView(v, nil, ref, nil, nil, (num: ref.value.size));
					vi.view.action= vi.view.action+{vi.save};
					vi;
				}.dup(nOut);
			};

			win.view.decorator.nextLine;
			outNumbers= {|i|
				var v= RedStaticText(win, nil, "o"++i);
				var w= RedGUICVMultiSliderView.defaultWidth;
				v.bounds= Rect(
					i*(w+gap.x)+(w*0.2)+savePosLeft,
					v.bounds.top,
					tabOffset,
					v.bounds.height
				);
			}.dup(nOut);

			win.onClose_({controllers.do{|x| x.remove}});
			CmdPeriod.doOnce({if(win.isClosed.not, {win.close})});
		}).play(AppClock);
	}
	close {
		if(win.isClosed.not, {win.close});
	}
}
