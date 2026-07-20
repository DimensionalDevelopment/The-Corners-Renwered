package net.ludocrypt.corners.util;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

public class RadioSoundTable {

	private final Holder<SoundEvent> musicSound;
	private final Holder<SoundEvent> staticSound;
	private final Holder<SoundEvent> radioSound;

	public RadioSoundTable(Holder<SoundEvent> musicSound, Holder<SoundEvent> staticSound,
			Holder<SoundEvent> radioSound) {
		this.musicSound = musicSound;
		this.staticSound = staticSound;
		this.radioSound = radioSound;
	}

	public Holder<SoundEvent> getMusicSound() {
		return musicSound;
	}

	public Holder<SoundEvent> getStaticSound() {
		return staticSound;
	}

	public Holder<SoundEvent> getRadioSound() {
		return radioSound;
	}
}
