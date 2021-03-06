package slimeknights.tconstruct.tools.item;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

import javax.annotation.Nonnull;

import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.AoeToolCore;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.tools.TinkerTools;

public class Shovel extends AoeToolCore {

  public static final ImmutableSet<net.minecraft.block.material.Material> effective_materials =
      ImmutableSet.of(net.minecraft.block.material.Material.GRASS,
                      net.minecraft.block.material.Material.GROUND,
                      net.minecraft.block.material.Material.SAND,
                      net.minecraft.block.material.Material.CRAFTED_SNOW,
                      net.minecraft.block.material.Material.SNOW,
                      net.minecraft.block.material.Material.CLAY,
                      net.minecraft.block.material.Material.CAKE);

  public Shovel() {
    this(PartMaterialType.handle(TinkerTools.toolRod),
         PartMaterialType.head(TinkerTools.shovelHead),
         PartMaterialType.extra(TinkerTools.binding));
  }

  protected Shovel(PartMaterialType... requiredComponents) {
    super(requiredComponents);

    addCategory(Category.HARVEST);

    setHarvestLevel("shovel", 0);
  }

  @Override
  public boolean isEffective(IBlockState block) {
    return effective_materials.contains(block.getMaterial()) || ItemSpade.EFFECTIVE_ON.contains(block);
  }
  
  // grass paths
  @Nonnull
  @Override
  public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
    if(ToolHelper.isBroken(stack)) {
      return EnumActionResult.FAIL;
    }
    
    EnumActionResult result = Items.DIAMOND_SHOVEL.onItemUse(stack, player, world, pos, hand, facing, hitX, hitY, hitZ);
    
    // only do the AOE path if the selected block is grass or grass path
    Block block = world.getBlockState(pos).getBlock();
    if(block == Blocks.GRASS || block == Blocks.GRASS_PATH) {
      for(BlockPos aoePos : getAOEBlocks(stack, world, player, pos)) {
        // stop if the tool breaks during the process
        if(ToolHelper.isBroken(stack)) {
          break;
        }
        
        EnumActionResult aoeResult = Items.DIAMOND_SHOVEL.onItemUse(stack, player, world, aoePos, hand, facing, hitX, hitY, hitZ);
        // if we pass on an earlier block, check if another block succeeds here instead
        if(result != EnumActionResult.SUCCESS) {
          result = aoeResult;
        }
      }
    }
    
    return result;
  }

  @Override
  public double attackSpeed() {
    return 1f;
  }

  @Override
  public float damagePotential() {
    return 0.9f;
  }

  @Override
  public NBTTagCompound buildTag(List<Material> materials) {
    return buildDefaultTag(materials).get();
  }
}
