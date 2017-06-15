package sifive.blocks.devices.xilinxvu190xdma

import Chisel._

import diplomacy.{LazyModule, LazyMultiIOModuleImp}
import rocketchip.HasSystemNetworks

trait HasPeripheryXilinxVU190XDMA extends HasSystemNetworks {
  val module: HasPeripheryXilinxVU190XDMAModuleImp

  val xilinxvu190xdma = LazyModule(new XilinxVU190XDMA)
  require(nMemoryChannels == 1, "Coreplex must have 1 master memory port")
  xilinxvu190xdma.node := mem(0).node
}

trait HasPeripheryXilinxVU190XDMABundle {
  val xilinxvu190xdma: XilinxVU190XDMAIO
  def connectXilinxVU190XDMAToPads(pads: XilinxVU190XDMAPads) {
    pads <> xilinxvu190xdma
  }
}

trait HasPeripheryXilinxVU190XDMAModuleImp extends LazyMultiIOModuleImp {
  val outer: HasPeripheryXilinxVU190XDMA
  val xilinxvu190xdma = IO(new XilinxVU190XDMAIO)

  xilinxvu190xdma <> outer.xilinxvu190xdma.module.io.port
}
