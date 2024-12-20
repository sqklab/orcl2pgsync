export interface IRouterItem {
    id: string;
    name: string;
    path: string;
    icon: string;
}

export interface IRouter extends IRouterItem {
    children: IRouterItem[]
}