//redFrik

//--todo:
//rewrite using Layout Manager
//use controlspecs from RedMix objects

//--related:
//RedAbstractMix RedEffectModuleGUI RedMixerGUI RedMatrixMixerGUI RedTapTempoGUI

RedMixGUI {
	var <redMix, <parent, position,
	win;
	*new {|redMix, parent, position|
		^super.newCopyArgs(redMix, parent, position).initRedMixGUI;
	}
	initRedMixGUI {
		var cmp= this.prContainer;
		var inA, inAController;
		var inB, inBController;
		var out, outController;
		var lag, lagController;
		var mixamp, mixController, ampController;
		var mixSpec= ControlSpec(-1, 1, 'lin', 0, -1);

		inA= RedNumberBox(cmp);
		inA.value= redMix.inA;
		inA.action= {|view| redMix.inA= view.value.round.max(0)};
		inAController= SimpleController(redMix.cvs[\inA]).put(\value, {|ref| inA.value= ref.value});
		inA.onClose= {inAController.remove};
		RedStaticText(cmp, "inA ("++redMix.def.metadata.info.inA++")");

		cmp.decorator.nextLine;
		inB= RedNumberBox(cmp);
		inB.value= redMix.inB;
		inB.action= {|view| redMix.inB= view.value.round.max(0)};
		inBController= SimpleController(redMix.cvs[\inB]).put(\value, {|ref| inB.value= ref.value});
		inB.onClose= {inBController.remove};
		RedStaticText(cmp, "inB ("++redMix.def.metadata.info.inB++")");

		cmp.decorator.nextLine;
		out= RedNumberBox(cmp);
		out.value= redMix.out;
		out.action= {|view| redMix.out= view.value.round.max(0)};
		outController= SimpleController(redMix.cvs[\out]).put(\value, {|ref| out.value= ref.value});
		out.onClose= {outController.remove};
		RedStaticText(cmp, "out (stereo)");

		cmp.decorator.nextLine;
		lag= RedNumberBox(cmp);
		lag.value= redMix.lag;
		lag.action= {|view| redMix.lag= view.value.max(0)};
		lagController= SimpleController(redMix.cvs[\lag]).put(\value, {|ref| lag.value= ref.value});
		lag.onClose= {lagController.remove};
		RedStaticText(cmp, "lag");

		cmp.decorator.nextLine;
		mixamp= Red2DSlider(cmp, cmp.decorator.indentedRemaining.extent);
		mixamp.x= mixSpec.unmap(redMix.mix);
		mixamp.y= redMix.amp;
		mixamp.action= {|view| redMix.mix= mixSpec.map(view.x); redMix.amp= view.y};
		mixController= SimpleController(redMix.cvs[\mix]).put(\value, {|ref| mixamp.x= mixSpec.unmap(ref.value)});
		ampController= SimpleController(redMix.cvs[\amp]).put(\value, {|ref| mixamp.y= ref.value});
		mixamp.onClose= {mixController.remove; ampController.remove};
		mixamp.mouseUpAction= {|view, x, y, mod| if(mod.isCtrl, {{redMix.mix= 0}.defer(0.1)})};
	}
	close {
		if(win.notNil and:{win.isClosed.not}, {win.close});
	}

	//--private
	prContainer {
		var cmp, width, height, margin= Point(4, 4), gap= Point(4, 4);
		position= position ?? {Point(600, 400)};
		width= 120;
		height= 140;
		if(parent.isNil, {
			parent= Window(redMix.class.name, Rect(position.x, position.y, width, height), false);
			win= parent;
			if(Main.versionAtMost(3, 4) and:{GUI.scheme!=\cocoa}, {
				win.alpha= GUI.skins.redFrik.unfocus;
			});
			win.front;
			CmdPeriod.doOnce({if(win.isClosed.not, {win.close})});
		});
		cmp= CompositeView(parent, Point(width, height))
		.background_(GUI.skins.redFrik.background);
		cmp.decorator= FlowLayout(cmp.bounds, margin, gap);
		^cmp;
	}
}
