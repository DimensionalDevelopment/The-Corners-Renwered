package org.dimdev.corners.config;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import org.dimdev.limlib.api.Config;

public class CornerConfig extends Config {


	public boolean delayMusicWithRadio = true;
	public boolean disableStrongShaders = false;

	public Christmas christmas = new Christmas();

	public static class Christmas {

		public boolean christmas = false;
		public List<String> leftColors = Lists.newArrayList("#30FF99", "#FE515C", "#FFFFFF");
		public List<String> rightColors = Lists.newArrayList("#FE515C", "#FFFFFF", "#30FF99");

		public boolean isChristmas() {
			return christmas || (LocalDate.now().getMonth() == Month.DECEMBER) || (LocalDate
				.now()
				.getMonth() == Month.JANUARY && LocalDate.now().getDayOfMonth() < 7);
		}

	}
}
