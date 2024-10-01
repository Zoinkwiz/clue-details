package com.cluedetails;

import java.util.List;
import lombok.Data;

@Data
public class ClueIdToInfo
{
	int id;

	String text;

	List<Integer> itemIds;

	public ClueIdToInfo(int id, String text)
	{
		this.text = text;
		this.id = id;
	}

	public ClueIdToInfo(int id, List<Integer> itemIds)
	{
		this.text = text;
		this.itemIds = itemIds;
	}

	public ClueIdToInfo(int id, String text, List<Integer> itemIds)
	{
		this.text = text;
		this.id = id;
		this.itemIds = itemIds;
	}
}
