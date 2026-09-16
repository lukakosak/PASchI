import {IModelDtoMapper} from "@/dto/mapper/IModelDtoMapper";

export const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";

export abstract class BaseService<Entity, Dto> {

  private readonly mapper: IModelDtoMapper<Entity, Dto>;

  protected constructor(mapper: IModelDtoMapper<Entity, Dto>) {
    this.mapper = mapper;
  }

  protected getMapper(): IModelDtoMapper<Entity, Dto> {
    return this.mapper;
  }

  abstract add(e: Entity): void;

  abstract update(e: Entity): void;

  abstract getById(id: string): Promise<Entity | undefined>;

  abstract getAll(): Promise<Entity[]>;

  abstract delete(id: string): void;
}
