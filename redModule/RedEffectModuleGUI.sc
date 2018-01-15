//redFrik

//related:
//RedEffectModule RedMixGUI RedMixerChannelGUI

RedEffectModuleGUI {
	var <redEffectModule, <parent, position,
	win, <views, <cmp, <params;
	*new {|redEffectModule, parent, position|
		^super.newCopyArgs(redEffectModule, parent, position).initRedEffectModuleGUI;
	}
	initRedEffectModuleGUI {
		var savedLeft= 0;
		params= redEffectModule.def.metadata[\order].reject{|x| x.key==\out};
		cmp= this.prContainer(params.size-1);
		views= List.new;
		params.do{|assoc|
			var k= assoc.key;
			var v= assoc.value;
			if(k==\mix, {
				views.add(
					RedGUICVSlider(
						cmp,
						nil,
						redEffectModule.cvs[v],
						{|x| redEffectModule.specs[k].map(x)},
						{|x| redEffectModule.specs[k].unmap(x)}
					)
				);
				views.last.view.mouseUpAction_{|view, x, y, mod|
					if(mod.isCtrl, {	//ctrl to center dry/wet mix
						redEffectModule.cvs[v].value_(0).changed(\value);
					});
				};
				savedLeft= cmp.decorator.left;
			}, {
				cmp.decorator.left= savedLeft;
				cmp.decorator.top= cmp.decorator.gap.y;
				views.add(
					RedGUICVKnob(
						cmp,
						nil,
						redEffectModule.cvs[v],
						{|x| redEffectModule.specs[k].map(x)},
						{|x| redEffectModule.specs[k].unmap(x)}
					)
				);
				savedLeft= cmp.decorator.left;
				cmp.decorator.shift(
					0-RedGUICVKnob.defaultWidth,
					RedGUICVKnob.defaultHeight
				);
				views.add(
					RedGUICVNumberBox(
						cmp,
						nil,
						redEffectModule.cvs[v],
						{|x| redEffectModule.specs[k].constrain(x)},
						{|x| redEffectModule.specs[k].constrain(x)}
					)
				);
				v= v.asString;
				cmp.decorator.shift(
					2-RedGUICVNumberBox.defaultWidth-(v.bounds(RedFont.new).width/5)*1.1,
					RedGUICVNumberBox.defaultHeight
				);
				RedStaticText(cmp, nil, v);
			});
		};
	}
	close {
		if(win.notNil and:{win.isClosed.not}, {win.close});
	}
	onClose_ {|func|
		win.onClose= func;
	}

	//--private
	prContainer {|num|
		var cmp, width, height, margin= Point(4, 4), gap= Point(4, 4);
		position= position ?? {Point(400, 400)};
		width= margin.x*2+RedGUICVSlider.defaultWidth+(RedGUICVKnob.defaultWidth+gap.x*num);
		height= margin.y*2+RedGUICVSlider.defaultHeight;
		if(parent.isNil, {
			win= Window(
				redEffectModule.class.name,
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
