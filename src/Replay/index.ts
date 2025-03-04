import { Match } from 'dimensions-ai/lib/main/Match';
import JSZip from 'jszip';
import { MatchEngine } from 'dimensions-ai/lib/main/MatchEngine';
import fs from 'fs';
import path from 'path';
import { GameMap } from '../GameMap';
import { Unit } from '../Unit';
import { State } from '../types';

export class Replay {
  public replayFilePath: string = null;
  public data: {
    seed: number;
    map: Array<
      Array<{
        ppt: number;
      }>
    >;
    bases: Array<{ x: number; y: number; team: Unit.TEAM }>;
    allCommands: Array<Array<MatchEngine.Command>>;
    agents: Array<{
      name: string;
      terminatedTurn: number;
    }>;
  } = {
    seed: undefined,
    map: [],
    bases: [],
    allCommands: [],
    agents: [],
  };
  constructor(match: Match, public compressReplay: boolean) {
    const d = new Date().valueOf();
    let replayFileName = `${d}_${match.id}`;
    if (compressReplay) {
      replayFileName += '.replay';
    } else {
      replayFileName += '.json';
    }
    this.replayFilePath = path.join(
      match.configs.storeReplayDirectory,
      replayFileName
    );
    if (!fs.existsSync(match.configs.storeReplayDirectory)) {
      fs.mkdirSync(match.configs.storeReplayDirectory, { recursive: true });
    }
    fs.writeFileSync(this.replayFilePath, '');
  }
  public writeMap(gameMap: GameMap): void {
    for (let y = 0; y < gameMap.width; y++) {
      this.data.map.push(
        gameMap.getRow(y).map((tile) => {
          return {
            ppt: tile.pointsPerTurn,
          };
        })
      );
    }
    gameMap.bases.forEach((base) => {
      this.data.bases.push({
        x: base.pos.x,
        y: base.pos.y,
        team: base.team,
      });
    });
  }
  public writeOut(match: Match): void {
    const state: State = match.state;
    this.data.agents = [{
      terminatedTurn: -1,
      name: match.agents[0].name,
    }, {
      terminatedTurn: -1,
      name: match.agents[1].name
    }]
    Unit.ALL_TEAMS.forEach((team) => {
      if (match.agents[team].isTerminated()) {
        this.data.agents[team].terminatedTurn = state.game.state.turn;
      }
    });
    if (this.compressReplay) {
      const zipper = new JSZip();
      zipper.file(this.replayFilePath, JSON.stringify(this.data));
      zipper
        .generateAsync({
          type: 'nodebuffer',
          compression: 'DEFLATE',
          compressionOptions: {
            level: 9,
          },
        })
        .then((content) => {
          fs.appendFileSync(this.replayFilePath, content);
        });
    } else {
      fs.appendFileSync(this.replayFilePath, JSON.stringify(this.data));
    }
  }
}
