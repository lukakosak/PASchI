import {User} from "@/model/User";
import {useUserStore} from "@/store/UserStore";
import {Role} from "@/model/Role";
import {UserService} from "@/service/UserService";
import {CategoryController} from "@/controller/CategoryController";
import {useStudentStore} from "@/store/ParticipantStore";
import {useCourseStore} from "@/store/CourseStore";
import {useSessionStore} from "@/store/SessionStore";
import {useCategoryStore} from "@/store/CategoryStore";
import {useInteractionStore} from "@/store/InteractionStore";
import {useSeatArrangementStore} from "@/store/SeatArrangementStore";
import {useRoomObjectStore} from "@/store/RoomObjectStore";
import {useRoomStore} from "@/store/RoomStore";
import {usePositionStore} from "@/store/PositionStore";
import {ParticipantService} from "@/service/ParticipantService";
import {Teacher} from "@/model/userdata/interactions/Teacher";
import {CourseService} from "@/service/CourseService";
import {RoomService} from "@/service/RoomService";
import {SessionService} from "@/service/SessionService";

export const LOGIN_SUCCESS = "Login succesful";
export const REGISTER_SUCCESS = "Register successful";
export const NO_TOKEN_ERROR = "No token found";
export const PASSWORD_ERROR = "Passwörter stimmen nicht überein";

/**
 * Steuert den Kontrollfluss für die Benutzerverwaltung.
 */
export class UserController {

  private static controller: UserController = new UserController();
  private userService = UserService.getService();

  private constructor() {
  }

  static getUserController(): UserController {
    return this.controller;
  }

  /**
   * Einloggen mit E-Mail und Passwort.
   *
   * @param email E-Mail
   * @param password Passwort
   */
  async login(email: string, password: string): Promise<string> {
    let user: User;
    try {
      user = await this.userService.login(email, password);
    } catch (message) {
      return <string>message;
    }

    useUserStore().setUser(user);
    if (!user.isAdmin()) {
      await this.getData();
    }
    return LOGIN_SUCCESS;
  }

  /**
   * Einloggen mit gültigem Token.
   */
  async loginWithToken(token: string | null): Promise<string> {
    if (token == null) {
      token = localStorage.getItem("token");
    }
    if (token == null) {
      return NO_TOKEN_ERROR;
    }
    let user: User;
    try {
      user = await this.userService.getToken(token);
    } catch (message) {
      return <string>message;
    }

    useUserStore().setUser(user);
    if (!user.isAdmin()) {
      await this.getData();
    }
    return LOGIN_SUCCESS;
  }

  /**
   * Meldet den aktuellen Benutzer ab.
   */
  logout() {
    this.clearStores();
    localStorage.clear();
  }

  /**
   * Account-Registrierung
   *
   * @param firstName Vorname
   * @param lastName Nachname
   * @param email E-Mail
   * @param password Passwort
   * @param repeatPassword Passwort
   */
  async register(firstName: string, lastName: string, email: string, password: string, repeatPassword: string):
    Promise<string> {

    if (password !== repeatPassword) {
      return PASSWORD_ERROR;
    }
    const user = new User(
      undefined,
      firstName,
      lastName,
      email,
      password,
      true,
      Role.USER,
      ""
    );
    try {
      await this.userService.add(user);
    } catch (message) {
      return <string>message;
    }
    user.deletePassword();
    return REGISTER_SUCCESS;
  }

  /**
   * Aktualisieren der Benutzerdaten.
   *
   * @param firstName Vorname
   * @param lastName Nachname
   */
  async update(firstName: string, lastName: string) {
    const user = this.getUser();
    if (user) {
      user.firstName = firstName;
      user.lastName = lastName;
      await this.userService.update(user);
      const teacher = useStudentStore().getTeacher();
      if (teacher) {
        teacher.firstName = firstName;
        teacher.lastName = lastName;
        await ParticipantService.getService().update(teacher);
      }
    }
  }

  /**
   * Gibt den aktuellen Benutzer zurück.
   */
  getUser(): User {
    return useUserStore().getUser()!;
  }

  /**
   * Gibt zurück, ob man aktuell angemeldet ist.
   */
  isLoggedIn(): boolean {
    return useUserStore().isLoggedIn();
  }

  private async getData() {
    await this.getTeacher();
    await CourseService.getService().getAll();
    await SessionService.getService().getAll();
    await RoomService.getService().getAll();
    await CategoryController.getCategoryController().fetchCategories();
  }

  private clearStores() {
    useUserStore().$reset();
    useStudentStore().$reset();
    useCourseStore().$reset();
    useSessionStore().$reset();
    useCategoryStore().$reset();
    useInteractionStore().$reset();
    useSeatArrangementStore().$reset();
    useRoomObjectStore().$reset();
    useRoomStore().$reset();
    usePositionStore().$reset();
  }

  private async getTeacher() {
    await ParticipantService.getService().getAll();
    let teacher = useStudentStore().getTeacher();
    if (teacher == undefined) {
      const user = this.getUser();
      teacher = new Teacher(
        undefined,
        useStudentStore().getNextId(),
        user,
        user.firstName,
        user.lastName
      );
      await ParticipantService.getService().add(teacher);
      useStudentStore().setTeacher(teacher);
    }
  }
}
