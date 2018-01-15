//redFrik

//--related:
//RedMixerChannel RedMixerGUI RedEffectModuleGUI

//todo: numberbox for out bus?
//todo: how to deal with inserts and archiving?

RedMixerChannelGUI {
	classvar <>width= 110, <>height= 280, <>numInserts= 2;
	var <redMixerChannel, <parent, position,
	win, <views, <cmp, textView,
	insertEfxs, insertWins;
	*new {|redMixerChannel, parent, position, name|
		^super.newCopyArgs(redMixerChannel, parent, position).initRedMixerChannelGUI(name);
	}
	initRedMixerChannelGUI {|name|
		var tmp, tmpWidth;
		var peakButtons, peakLevels;
		var volSpec= #[-90, 6, \db].asSpec;
		var volWarp= DbFaderWarp(volSpec);
		var freqSpec= \freq.asSpec;
		var bandSpec= ControlSpec(0.25, 2, 'exp', 0, 1);
		var gainSpec= #[-40, 20, \db, 0, 0].asSpec;
		var controllers= List.new;
		cmp= this.prContainer;

		//--effect inserts
		insertEfxs= Array.newClear(numInserts);
		insertWins= Array.newClear(numInserts);
		numInserts.do{|i|
			var popup, button;
			tmp= Point(cmp.bounds.width*0.7, 14);
			popup= RedPopUpMenu(cmp, tmp)
			.items_(["_inserts_"]++RedEffectModule.subclasses.collect{|x| x.name});
			tmp= Point(cmp.decorator.indentedRemaining.width, 14);
			button= RedButton(cmp, tmp, "o", "o")
			.action_{|view|
				var efx, win;
				if(view.value==1, {
					if(popup.value>0, {
						Routine({
							var pos;
							efx= RedEffectModule.subclasses[popup.value-1].new;
							redMixerChannel.insert(efx, #[\addToHead, \addToTail].clipAt(i));
							pos= Point(parent.bounds.right, parent.bounds.bottom-80-(i*110));
							win= efx.gui(nil, pos);
							insertEfxs[i]= efx;
							insertWins[i]= win;
							win.onClose= {
								redMixerChannel.remove(insertEfxs[i]);
								insertEfxs[i]= nil;
								view.value= 0;
							};
						}).play(AppClock);
					});
				}, {
					if(insertWins[i].notNil, {
						insertWins[i].close;
						insertWins[i]= nil;
					});
				});
			};
			cmp.decorator.nextLine;
		};

		//--equaliser
		views= List.new;
		views.add(
			RedGUICVKnob(cmp, Point(20, 20), redMixerChannel.cvs.hiFreq,
				{|x| freqSpec.map(x)}, {|x| freqSpec.unmap(x)})
		);
		views.add(
			RedGUICVKnob(cmp, Point(20, 20), redMixerChannel.cvs.hiBand,
				{|x| bandSpec.map(x)}, {|x| bandSpec.unmap(x)})
		);
		views.add(
			RedGUICVKnob(cmp, Point(20, 20), redMixerChannel.cvs.hiGain,
				{|x| gainSpec.map(x)}, {|x| gainSpec.unmap(x)})
		);
		tmp= Point(cmp.decorator.indentedRemaining.width, 14);
		views.add(
			RedGUICVButton(cmp, tmp, redMixerChannel.cvs.eqHi, nil, nil, (str: "hi"))
		);
		cmp.decorator.nextLine;

		views.add(
			RedGUICVKnob(cmp, Point(20, 20), redMixerChannel.cvs.miFreq,
				{|x| freqSpec.map(x)}, {|x| freqSpec.unmap(x)})
		);
		views.add(
			RedGUICVKnob(cmp, Point(20, 20), redMixerChannel.cvs.miBand,
				{|x| bandSpec.map(x)}, {|x| bandSpec.unmap(x)})
		);
		views.add(
			RedGUICVKnob(cmp, Point(20, 20), redMixerChannel.cvs.miGain,
				{|x| gainSpec.map(x)}, {|x| gainSpec.unmap(x)})
		);
		tmp= Point(cmp.decorator.indentedRemaining.width, 14);
		views.add(
			RedGUICVButton(cmp, tmp, redMixerChannel.cvs.eqMi, nil, nil, (str: "mi"))
		);
		cmp.decorator.nextLine;

		views.add(
			RedGUICVKnob(cmp, Point(20, 20), redMixerChannel.cvs.loFreq,
				{|x| freqSpec.map(x)}, {|x| freqSpec.unmap(x)})
		);
		views.add(
			RedGUICVKnob(cmp, Point(20, 20), redMixerChannel.cvs.loBand,
				{|x| bandSpec.map(x)}, {|x| bandSpec.unmap(x)})
		);
		views.add(
			RedGUICVKnob(cmp, Point(20, 20), redMixerChannel.cvs.loGain,
				{|x| gainSpec.map(x)}, {|x| gainSpec.unmap(x)})
		);
		tmp= Point(cmp.decorator.indentedRemaining.width, 14);
		views.add(
			RedGUICVButton(cmp, tmp, redMixerChannel.cvs.eqLo, nil, nil, (str: "lo"))
		);
		cmp.decorator.nextLine;

		//--balance
		views.add(
			RedGUICVSlider(
				cmp,
				Point(cmp.bounds.width*0.7, 14),
				redMixerChannel.cvs.bal,
				{|x| x*2-1},
				{|x| x*0.5+0.5}
			)
		);
		views.last.view.mouseUpAction_{|view, x, y, mod|
			if(mod.isCtrl, {	//ctrl to center balance
				{redMixerChannel.bal= 0}.defer(0.1);
			});
		};

		//--mute
		tmp= Point(cmp.decorator.indentedRemaining.width, 14);
		views.add(
			RedGUICVButton(
				cmp,
				tmp,
				redMixerChannel.cvs.mute,
				nil,
				nil,
				(str: "m")
			)
		);
		cmp.decorator.nextLine;

		//--volume number
		tmp= cmp.decorator.indentedRemaining;
		views.add(
			RedGUICVNumberBox(
				cmp,
				Point(tmp.width*0.45, RedGUICVNumberBox.defaultHeight),
				redMixerChannel.cvs.amp,
				{|x| x.dbamp},
				{|x| x.ampdb}
			)
		);

		//--peaks
		CompositeView(cmp, Point(12, RedGUICVNumberBox.defaultHeight));	//dummy spacer
		tmpWidth= cmp.decorator.indentedRemaining.width*0.5-(cmp.decorator.gap.x/2);
		peakButtons= [
			RedButton(cmp, Point(tmpWidth, RedGUICVNumberBox.defaultHeight), "", ""),
			RedButton(cmp, Point(tmpWidth, RedGUICVNumberBox.defaultHeight), "", "")
		];
		peakButtons.do{|x|
			x.canFocus_(false);
		};
		controllers.add(
			SimpleController(redMixerChannel.cvs.peaks).put(\value, {|ref|
				defer{
					ref.value.do{|x, i|
						if(x>1 and:{peakButtons[i].value==0}, {
							peakButtons[i].value= 1;
						}, {
							if(x<=1 and:{peakButtons[i].value==1}, {
								peakButtons[i].value= 0;
							});
						});
					};
				};
			})
		);
		cmp.decorator.nextLine;

		//--volume slider
		tmp= cmp.decorator.indentedRemaining;
		if(name.notNil, {
			tmp= tmp-Point(0, RedGUICVNumberBox.defaultHeight-cmp.decorator.gap.y);
		});
		views.add(
			RedGUICVSlider(
				cmp,
				Point(tmp.width*0.45, tmp.height),
				redMixerChannel.cvs.amp,
				{|x| volWarp.map(x).dbamp},
				{|x| volWarp.unmap(x.ampdb)}
			)
		);
		views.last.view.mouseUpAction_{|view, x, y, mod|
			if(mod.isCtrl, {	//ctrl to reset volume
				{redMixerChannel.vol= 0}.defer(0.1);
			});
		};

		//--unity gain marking
		RedStaticText(
			CompositeView(cmp, Point(12, tmp.height)),
			Rect(0, 1-volSpec.unmap(0)*tmp.height, 12, 12),
			"-u"
		);

		//--levels
		peakLevels= [
			RedLevelIndicator(cmp, Point(tmpWidth, tmp.height)),
			RedLevelIndicator(cmp, Point(tmpWidth, tmp.height))
		];
		peakLevels.do{|x|
			x.canFocus_(false);
			x.warning_(volSpec.unmap(-3));
			x.critical_(volSpec.unmap(0));
		};
		controllers.add(
			SimpleController(redMixerChannel.cvs.levels).put(\value, {|ref|
				defer{
					ref.value.do{|x, i| peakLevels[i].value= volSpec.unmap(x.ampdb)};
				};
			})
		);

		//--name
		if(name.notNil, {
			cmp.decorator.nextLine;
			tmp= name.bounds(RedFont.new).width;
			cmp.decorator.shift((cmp.bounds.width-tmp*0.25).max(0), -4);
			textView= RedStaticText(cmp, nil, name);
		});

		cmp.onClose_({controllers.do{|x| x.remove}});
	}
	text_ {|str|
		var tmp, tmp2;
		if(textView.notNil, {
			tmp= textView.string.bounds(RedFont.new).width;
			tmp2= str.bounds(RedFont.new).width+2;
			textView.bounds= Rect(
				textView.bounds.left-(tmp2-tmp/4),
				textView.bounds.top,
				tmp2,
				textView.bounds.height
			);
			textView.string= str;
		});
	}
	close {
		insertWins.do{|x| if(x.notNil, {x.close})};
		if(win.notNil and:{win.isClosed.not}, {win.close});
	}

	//--private
	prContainer {
		var cmp, width, height, margin= Point(4, 4), gap= Point(4, 4);
		position= position ?? {Point(500, 500)};
		width= RedMixerChannelGUI.width;
		height= RedMixerChannelGUI.height;
		if(parent.isNil, {
			win= Window(
				redMixerChannel.class.name,
				Rect(position.x, position.y, width, height),
				false
			);
			parent= win;
			win.front;
			CmdPeriod.doOnce({if(win.isClosed.not, {win.close})});
		});
		cmp= CompositeView(parent, Point(width, height))
		.background_(GUI.skins.redFrik.background);
		cmp.decorator= FlowLayout(cmp.bounds, margin, gap);
		cmp.onClose= {this.close};
		^cmp;
	}
}
