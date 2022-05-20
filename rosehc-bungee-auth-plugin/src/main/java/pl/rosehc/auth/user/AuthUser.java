package pl.rosehc.auth.user;

import java.util.Objects;
import pl.rosehc.controller.wrapper.auth.AuthUserSerializableWrapper;

public final class AuthUser {

  private final String nickname;
  private final long firstJoinTime;

  private String password, lastIP;
  private boolean premium, registered, logged, blazingAuthenticated, kickedFromServer;
  private long lastOnlineTime, timeout;

  public AuthUser(final String nickname, final String lastIP, final long firstJoinTime,
      final long lastOnlineTime, final boolean premium) {
    this.nickname = nickname;
    this.lastIP = lastIP;
    this.premium = premium;
    this.firstJoinTime = firstJoinTime;
    this.lastOnlineTime = lastOnlineTime;
    this.registered = premium;
  }

  private AuthUser(final AuthUserSerializableWrapper wrapper) {
    this.nickname = wrapper.getNickname();
    this.password = wrapper.getPassword();
    this.lastIP = wrapper.getLastIP();
    this.firstJoinTime = wrapper.getFirstJoinTime();
    this.lastOnlineTime = wrapper.getLastOnlineTime();
    this.premium = wrapper.isPremium();
    this.registered = wrapper.isRegistered();
  }

  public static AuthUser create(final AuthUserSerializableWrapper wrapper) {
    return new AuthUser(wrapper);
  }

  public String getNickname() {
    return this.nickname;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public String getLastIP() {
    return lastIP;
  }

  public void setLastIP(final String lastIP) {
    this.lastIP = lastIP;
  }

  public boolean isPremium() {
    return this.premium;
  }

  public void setPremium(final boolean premium) {
    this.premium = premium;
  }

  public boolean isRegistered() {
    return this.registered;
  }

  public void setRegistered(final boolean registered) {
    this.registered = registered;
  }

  public boolean isLogged() {
    return this.logged;
  }

  public void setLogged(final boolean logged) {
    this.logged = logged;
  }

  public boolean isBlazingAuthenticated() {
    return this.blazingAuthenticated;
  }

  public void setBlazingAuthenticated(final boolean blazingAuthenticated) {
    this.blazingAuthenticated = blazingAuthenticated;
  }

  public boolean isKickedFromServer() {
    final boolean kickedFromServer = this.kickedFromServer;
    this.kickedFromServer = false;
    return kickedFromServer;
  }

  public void setKickedFromServer(final boolean kickedFromServer) {
    this.kickedFromServer = kickedFromServer;
  }

  public long getFirstJoinTime() {
    return this.firstJoinTime;
  }

  public long getLastOnlineTime() {
    return this.lastOnlineTime;
  }

  public void setLastOnlineTime(final long lastOnlineTime) {
    this.lastOnlineTime = lastOnlineTime;
  }

  public long getTimeout() {
    return this.timeout;
  }

  boolean hasTimeout() {
    return this.timeout > System.currentTimeMillis();
  }

  public void resetTimeout() {
    this.timeout = System.currentTimeMillis() + 30_000L;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }

    if (other == null || this.getClass() != other.getClass()) {
      return false;
    }

    AuthUser user = (AuthUser) other;
    return this.nickname.equals(user.nickname);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.nickname);
  }
}
