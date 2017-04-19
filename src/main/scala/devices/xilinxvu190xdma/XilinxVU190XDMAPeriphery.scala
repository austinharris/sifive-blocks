package sifive.blocks.devices.xilinxvu190xdma

import Chisel._

import diplomacy._
import rocketchip.{
  HasTopLevelNetworks,
  HasTopLevelNetworksModule,
  HasTopLevelNetworksBundle
}
import coreplex.BankedL2Config

trait HasPeripheryXilinxVU190XDMA extends HasTopLevelNetworks {
  val module: HasPeripheryXilinxVU190XDMAModule

  val xilinxvu190xdma = LazyModule(new XilinxVU190XDMA)
  require(p(BankedL2Config).nMemoryChannels == 1, "Coreplex must have 1 master memory port")
  xilinxvu190xdma.node := mem(0).node
}

trait HasPeripheryXilinxVU190XDMABundle extends HasTopLevelNetworksBundle {
  val xilinxvu190xdma = new XilinxVU190XDMAIO
}

trait HasPeripheryXilinxVU190XDMAModule extends HasTopLevelNetworksModule {
  val outer: HasPeripheryXilinxVU190XDMA
  val io: HasPeripheryXilinxVU190XDMABundle

  io.xilinxvu190xdma <> outer.xilinxvu190xdma.module.io.port
}
