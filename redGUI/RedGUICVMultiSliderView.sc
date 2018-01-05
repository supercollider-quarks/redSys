//redFrik

RedGUICVMultiSliderView {
	classvar <defaultWidth= 25,<defaultHeight= 25;
	var <ref, <multiSlider, <savedValues, multiSliderController;
	*new {|parent, bounds, ref|
		^super.new.init(parent, bounds, ref);
	}
	init {|parent, bounds, argRef|
		this.prMake(parent, bounds, argRef.value.size);
		multiSlider.value= argRef.value;
		this.prConnect(argRef);
	}
	interp {|val, target| this.value_(savedValues.blend(target, val))}
	value_ {|val| ref.value_(val).changed(\value)}
	value {^multiSlider.value}
	save {savedValues= multiSlider.value}

	//--private
	prMake {|parent, bounds, size|
		if(bounds.isNil, {
			bounds= Rect(0, 0, defaultWidth, defaultHeight*size);
		}, {
			bounds= bounds.asRect;
		});
		multiSlider= RedMultiSliderView(parent, bounds);
		multiSlider.indexIsHorizontal= false;
		multiSlider.indexThumbSize= multiSlider.bounds.height/size-4;
		multiSlider.valueThumbSize= 0;
		multiSlider.gap= 4;
		multiSlider.isFilled= true;
	}
	prConnect {|argRef|
		ref= argRef;
		multiSlider.mouseMoveAction= {|view|
			ref.value_(view.value).changed(\value);
		};
		multiSlider.mouseDownAction= multiSlider.mouseMoveAction;
		multiSliderController= SimpleController(ref).put(\value, {|ref|
			multiSlider.value= ref.value;
		});
		multiSlider.onClose_({multiSliderController.remove});
		this.save;
	}
}

RedGUICVMultiSliderViewMirror : RedGUICVMultiSliderView {
	prConnect {}
	value_ {|val| multiSlider.value= val}
}
