package pl.rosehc.guilds.ranking;

import pl.rosehc.guilds.GuildsPlugin;

public abstract class Ranking {

  private int points, kills, deaths;
  private int position;

  public Ranking(final int points, final int kills, final int deaths) {
    this.points = points;
    this.kills = kills;
    this.deaths = deaths;
  }

  public float getKDR() {
    if (this.kills == 0 && this.deaths == 0) {
      return 0F;
    }

    return this.kills <= 0 || this.deaths != 0 ? this.deaths <= 0 || this.kills != 0 ? this.kills
        / (float) this.deaths : -this.deaths : this.kills;
  }

  public int getPoints() {
    return this.points;
  }

  public void setPoints(final int points) {
    this.points = Math.max(points, 0);
  }

  public int getKills() {
    return this.kills;
  }

  public void setKills(final int kills) {
    this.kills = kills;
  }

  public int getDeaths() {
    return this.deaths;
  }

  public void setDeaths(final int deaths) {
    this.deaths = deaths;
  }

  public int getPosition() {
    return this.position;
  }

  public void setPosition(final int position) {
    this.position = position;
  }
}
