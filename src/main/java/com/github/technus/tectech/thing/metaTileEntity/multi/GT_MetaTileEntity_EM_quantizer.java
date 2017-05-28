package com.github.technus.tectech.thing.metaTileEntity.multi;

import com.github.technus.tectech.CommonValues;
import com.github.technus.tectech.TecTech;
import com.github.technus.tectech.auxiliary.TecTechConfig;
import com.github.technus.tectech.elementalMatter.classes.*;
import com.github.technus.tectech.elementalMatter.interfaces.iHasElementalDefinition;
import com.github.technus.tectech.thing.block.QuantumGlassBlock;
import com.github.technus.tectech.thing.metaTileEntity.iConstructible;
import gregtech.api.GregTech_API;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import static com.github.technus.tectech.Util.StructureBuilder;
import static com.github.technus.tectech.Util.isInputEqual;
import static com.github.technus.tectech.elementalMatter.definitions.dAtomDefinition.*;
import static com.github.technus.tectech.thing.casing.GT_Container_CasingsTT.sBlockCasingsTT;
import static gregtech.api.enums.GT_Values.V;

/**
 * Created by danie_000 on 17.12.2016.
 */
public class GT_MetaTileEntity_EM_quantizer extends GT_MetaTileEntity_MultiblockBase_EM implements iConstructible {
    //region Structure
    //use multi A energy inputs, use less power the longer it runs
    private static final String[][] shape = new String[][]{
            {"!!!", "!.!", "!!!",},
            {"010", "101", "010",},
            {"\"\"\"", "\"0\"", "\"\"\"",},
            {"202", "0 0", "202",},
    };
    private static final Block[] blockType = new Block[]{sBlockCasingsTT, sBlockCasingsTT, QuantumGlassBlock.INSTANCE};
    private static final byte[] blockMeta = new byte[]{4, 0, 0};
    private static final String[] addingMethods = new String[]{"addElementalOutputToMachineList", "addClassicToMachineList", "addElementalMufflerToMachineList"};
    private static final byte[] casingTextures = new byte[]{textureOffset + 4, textureOffset, textureOffset + 4};
    private static final Block[] blockTypeFallback = new Block[]{sBlockCasingsTT, sBlockCasingsTT, sBlockCasingsTT};
    private static final byte[] blockMetaFallback = new byte[]{4, 0, 4};
    //endregion

    public GT_MetaTileEntity_EM_quantizer(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_EM_quantizer(String aName) {
        super(aName);
    }

    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_EM_quantizer(this.mName);
    }

    @Override
    public boolean EM_checkMachine(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        return EM_StructureCheckAdvanced(shape, blockType, blockMeta, addingMethods, casingTextures, blockTypeFallback, blockMetaFallback, 1, 1, 0);
    }

    @Override
    public void construct(int qty) {
        StructureBuilder(shape, blockType, blockMeta, 1, 1, 0, getBaseMetaTileEntity());
    }

    @Override
    public String[] getDescription() {
        return new String[]{
                CommonValues.tecMark,
                "Conveniently convert regular stuff into quantum form.",
                EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + "To make it more inconvenient."
        };
    }

    @Override
    public boolean EM_checkRecipe(ItemStack itemStack) {//TODO implement by item quantization, implement instance quantization
        if (GregTech_API.sPostloadFinished) {
            ItemStack[] inI = getStoredInputs().toArray(new ItemStack[0]);
            if (inI.length > 0) {
                for (ItemStack is : inI) {
                    //ITEM STACK quantization
                    aItemQuantizationInfo aIQI = bTransformationInfo.itemQuantization.get(new aItemQuantizationInfo(is, false, null));
                    if (aIQI == null) {
                        aIQI = bTransformationInfo.itemQuantization.get(new aItemQuantizationInfo(is, true, null));
                    }
                    if (aIQI == null) {
                        //ORE DICT quantization
                        int[] oreIDs = OreDictionary.getOreIDs(is);
                        for (int ID : oreIDs) {
                            if (TecTechConfig.DEBUG_MODE)
                                TecTech.Logger.info("Quantifier-Ore-recipe " + is.getItem().getUnlocalizedName() + "." + is.getItemDamage() + " " + OreDictionary.getOreName(ID));
                            aOredictQuantizationInfo aOQI = bTransformationInfo.oredictQuantization.get(ID);
                            if (aOQI == null) continue;
                            iHasElementalDefinition into = aOQI.output();
                            if (into != null && isInputEqual(true, false,
                                    nothingF, new ItemStack[]{new ItemStack(is.getItem(), aOQI.amount, is.getItemDamage())}, null, inI)) {
                                startRecipe(into);
                                return true;
                            }
                        }
                    } else {
                        //Do ITEM STACK quantization
                        if (TecTechConfig.DEBUG_MODE)
                            TecTech.Logger.info("Quantifier-Item-recipe " + is.getItem().getUnlocalizedName() + "." + is.getItemDamage());
                        iHasElementalDefinition into = aIQI.output();
                        if (into != null && isInputEqual(true, false,
                                nothingF, new ItemStack[]{new ItemStack(is.getItem(), aIQI.input().stackSize, is.getItemDamage())}, null, inI)) {
                            startRecipe(into);
                            return true;
                        }
                    }
                }
            }
            FluidStack[] inF = getStoredFluids().toArray(new FluidStack[0]);
            if (inF.length > 0) {
                for (FluidStack fs : inF) {
                    aFluidQuantizationInfo aFQI = bTransformationInfo.fluidQuantization.get(fs.getFluid().getID());
                    if (aFQI == null) continue;
                    iHasElementalDefinition into = aFQI.output();
                    if (into != null && fs.amount >= aFQI.input().amount && isInputEqual(true, false,
                            new FluidStack[]{aFQI.input()}, nothingI, inF, (ItemStack[]) null)) {
                        startRecipe(into);
                        return true;
                    }
                }
            }
        }
        mEfficiencyIncrease = 0;
        mMaxProgresstime = 0;
        return false;
    }

    private void startRecipe(iHasElementalDefinition into) {
        mMaxProgresstime = 20;
        mEfficiencyIncrease = 10000;
        float mass = into.getMass();
        float euMult = mass / refMass;
        eAmpereFlow = (int) Math.ceil(euMult);
        if (mass > refUnstableMass || into.getDefinition().getRawLifeTime()<1.5e25f) {
            mEUt = (int) -V[10];
        } else {
            mEUt = (int) -V[8];
        }
        outputEM = new cElementalInstanceStackMap[]{
                into instanceof cElementalInstanceStack ?
                        new cElementalInstanceStackMap((cElementalInstanceStack) into) :
                        new cElementalInstanceStackMap(new cElementalInstanceStack(into.getDefinition(), into.getAmount()))
        };
    }

    @Override
    public void EM_outputFunction() {
        if (eOutputHatches.size() < 1) {
            stopMachine();
            return;
        }
        eOutputHatches.get(0).getContainerHandler().putUnifyAll(outputEM[0]);
        outputEM=null;
    }
}
