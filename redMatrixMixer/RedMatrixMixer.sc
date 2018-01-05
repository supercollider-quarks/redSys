//redFrik

//--related:
//RedMatrixMixerGUI, RedEffectsRack, RedMixer

RedMatrixMixer {
	var <group,
	<isReady= false, groupPassedIn,
	<nIn, <nOut, <synth, <os, <defString;
	*new {|nIn= 8, nOut= 8, in= 0, out= 0, group, lag= 0.05|
		^super.new.initRedMatrixMixer(nIn, nOut, in, out, group, lag);
	}
	initRedMatrixMixer {|argNIn, argNOut, argIn, argOut, argGroup, argLag|
		var server;
		nIn= argNIn;
		nOut= argNOut;

		if(argGroup.notNil, {
			server= argGroup.server;
			groupPassedIn= true;
		}, {
			server= Server.default;
			groupPassedIn= false;
		});

		os= (
			in: Ref(argIn),
			out: Ref(argOut),
			lag: Ref(argLag)
		);
		SimpleController(os[\in]).put(\value, {|ref|
			synth.set(\in, ref.value);
		});
		SimpleController(os[\out]).put(\value, {|ref|
			synth.set(\out, ref.value);
		});
		SimpleController(os[\lag]).put(\value, {|ref|
			synth.set(\lag, ref.value);
		});

		forkIfNeeded{
			if(groupPassedIn.not, {
				server.bootSync;
				group= Group.after(server.defaultGroup);
				server.sync;
				CmdPeriod.doOnce({group.free});
			}, {
				group= argGroup;
			});

			//--send definitions
			this.def.add;
			server.sync;

			//--create synth
			synth= Synth(\redMatrixMixer, os.asKeyValuePairs, group, \addToTail);
			nOut.do{|i|
				var arr;
				var name= ("o"++i).asSymbol;
				var setName= ("o"++i++"_").asSymbol;
				this.addUniqueMethod(setName, {|mixer, arr|
					os[name].value_(arr).changed(\value);
				});
				this.addUniqueMethod(name, {|mixer|
					os[name].value;
				});
				arr= 0.dup(nIn);
				if(i<nIn, {arr= arr.put(i, 1)});
				os[name]= Ref(arr);
				SimpleController(os[name]).put(\value, {|ref|
					synth.set(name, ref.value);
					ref.changed;
				});
			};
			server.sync;
			isReady= true;
		};
	}
	def {
		defString= "SynthDef('redMatrixMixer', {|in= 0, out= 0, lag= 0.05";
		nOut.do{|i|
			var arr= 0.dup(nIn);
			if(i<nIn, {arr= arr.put(i, 1)});
			defString= defString++", o"++i++"= #"++arr;
		};
		defString= defString++"|\n\tvar z= In.ar(in, "++nIn++");";
		nOut.do{|i| defString= defString++"\n\tvar m"++i++"= Mix(z*Ramp.kr(o"++i++", lag));"};
		nOut.do{|i| defString= defString++"\n\tReplaceOut.ar(out+"++i++", m"++i++");"};
		defString= defString++"\n});";
		^defString.interpret;
	}
	in {^os[\in].value}
	in_ {|val|
		os[\in].value_(val).changed(\value);
	}
	out {^os[\out].value}
	out_ {|val|
		os[\out].value_(val).changed(\value);
	}
	lag {^os[\lag].value}
	lag_ {|val|
		os[\lag].value_(val).changed(\value);
	}
	free {
		synth.free;
		if(groupPassedIn.not, {group.free});
	}
	gui {|position|
		^RedMatrixMixerGUI(this, position);
	}
}
