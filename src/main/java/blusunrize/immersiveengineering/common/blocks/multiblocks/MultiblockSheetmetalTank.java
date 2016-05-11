package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySheetmetalTank;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDecoration;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MultiblockSheetmetalTank implements IMultiblock
{
	public static MultiblockSheetmetalTank instance = new MultiblockSheetmetalTank();

	static ItemStack[][][] structure = new ItemStack[5][3][3];
	static{
		for(int h=0;h<5;h++)
			for(int l=0;l<3;l++)
				for(int w=0;w<3;w++)
				{
					if(h==0)
					{
						if((l==0||l==2)&&(w==0||w==2))
							structure[h][l][w]=new ItemStack(IEContent.blockWoodenDecoration,1,BlockTypes_WoodenDecoration.FENCE.getMeta());
						else if(l==1&&w==1)
							structure[h][l][w]=new ItemStack(IEContent.blockSheetmetal,1,BlockTypes_MetalsAll.IRON.getMeta());
					}
					else if(h<1||h>3 || w!=1||l!=1)
						structure[h][l][w]=new ItemStack(IEContent.blockSheetmetal,1,BlockTypes_MetalsAll.IRON.getMeta());
				}
	}
	@Override
	public ItemStack[][][] getStructureManual()
	{
		return structure;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public boolean overwriteBlockRender(ItemStack stack, int iterator)
	{
		return false;
	}
	@Override
	public float getManualScale()
	{
		return 10;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}
	@SideOnly(Side.CLIENT)
	static ItemStack renderStack;
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		if(renderStack==null)
			renderStack = new ItemStack(IEContent.blockMetalMultiblock,1,BlockTypes_MetalMultiblock.TANK.getMeta());
		GlStateManager.scale(8,8,8);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.translate(0, .0625, 0);
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
	}

	@Override
	public String getUniqueName()
	{
		return "IE:SheetmetalTank";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		return Utils.compareToOreName(new ItemStack(state.getBlock(),1,state.getBlock().getMetaFromState(state)), "blockSheetmetalIron");
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		EnumFacing f = EnumFacing.fromAngle(player.rotationYaw);
		pos = pos.offset(f);
		if(!(Utils.isOreBlockAt(world, pos.offset(f,-1).offset(f.rotateY()), "fenceTreatedWood") && Utils.isOreBlockAt(world, pos.offset(f,-1).offset(f.rotateYCCW()), "fenceTreatedWood")))
			for(int i=0; i<4; i++)
				if(Utils.isOreBlockAt(world, pos.add(0,-i,0).offset(f,-1).offset(f.rotateY()), "fenceTreatedWood") && Utils.isOreBlockAt(world, pos.add(0,-i,0).offset(f,-1).offset(f.rotateYCCW()), "fenceTreatedWood"))
				{
					pos = pos.add(0,-i,0);
					break;
				}

		for(int h=0;h<=4;h++)
			for(int xx=-1;xx<=1;xx++)
				for(int zz=-1;zz<=1;zz++)
					if(h==0)
					{
						if(Math.abs(xx)==1&&Math.abs(zz)==1)
						{
							if(!Utils.isOreBlockAt(world, pos.add(xx, h, zz), "fenceTreatedWood"))
								return false;
						}
						else if(xx==0&&zz==0)
							if(!Utils.isOreBlockAt(world, pos.add(xx, h, zz), "blockSheetmetalIron"))
								return false;
					}
					else
					{
						if(h<4 && xx==0&&zz==0)
						{
							if(!world.isAirBlock(pos.add(xx, h, zz)))
								return false;
						}
						else if(!Utils.isOreBlockAt(world, pos.add(xx, h, zz), "blockSheetmetalIron"))
							return false;
					}

		for(int h=0;h<=4;h++)
			for(int l=-1;l<=1;l++)
				for(int w=-1;w<=1;w++)
				{
					if(h==0 && !((l==0&&w==0)||(Math.abs(l)==1&&Math.abs(w)==1)))
						continue;
					if(h>0&&h<4 && l==0&&w==0)
						continue;

					int xx = f==EnumFacing.EAST?l: f==EnumFacing.WEST?-l: f==EnumFacing.NORTH?-w:w;
					int zz = f==EnumFacing.NORTH?l: f==EnumFacing.SOUTH?-l: f==EnumFacing.EAST?w:-w;

					world.setBlockState(pos.add(xx, h, zz), IEContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_MetalMultiblock.TANK.getMeta()));
					TileEntity curr = world.getTileEntity(pos.add(xx, h, zz));
					if(curr instanceof TileEntitySheetmetalTank)
					{
						TileEntitySheetmetalTank currTank = (TileEntitySheetmetalTank) curr;
						currTank.offset=new int[]{xx,h,zz};
						currTank.pos = h*9 + (l+1)*3 + (w+1);
						currTank.facing=f.getOpposite();
						currTank.formed=true;
						currTank.offset=new int[]{xx,h,zz};
						currTank.markDirty();
					}
				}
		return true;
	}

	static final ItemStack[] materials = new ItemStack[]{new ItemStack(IEContent.blockWoodenDecoration,4,BlockTypes_WoodenDecoration.FENCE.getMeta()),new ItemStack(IEContent.blockSheetmetal,34,BlockTypes_MetalsAll.IRON.getMeta())};
	@Override
	public ItemStack[] getTotalMaterials()
	{
		return materials;
	}
}