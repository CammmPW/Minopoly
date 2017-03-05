package de.wolfi.minopoly.utils;

import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;

import de.robingrether.idisguise.disguise.Disguise;
import de.robingrether.idisguise.disguise.DisguiseType;

public enum FigureType {

	WOLF(DisguiseBuilder.create(DisguiseType.WOLF).setCollar(DyeColor.RED).create(), EntityType.WOLF),
	SHEEP(DisguiseBuilder.create(DisguiseType.SHEEP).setColor(DyeColor.BLUE).create(),EntityType.SHEEP);

	private Disguise d;
	private EntityType entityType;

	private FigureType(Disguise d, EntityType en) {
		this.d = d;
		this.entityType = en;
	}

	public Disguise createDisguise() {
		return this.d.clone();
	}

	public EntityType getEntityType() {
		return this.entityType;
	}

	public String getName() {
		return this.d.getType().toString();
	}
}
