import axios from 'axios'; import type {BoxBenchmark,Price,PricePage} from './types';
const configuredApi=import.meta.env.VITE_API_URL as string|undefined;
const api=axios.create({baseURL:configuredApi,timeout:15000});
export async function getPrices(params:Record<string,string|number|undefined>):Promise<PricePage>{
 if(configuredApi) return (await api.get<PricePage>('/prices/history',{params})).data;
 const response=await fetch(`${import.meta.env.BASE_URL}data/prices.json`,{cache:'no-cache'});
 if(!response.ok) throw new Error(`Static price data unavailable: ${response.status}`);
 const payload=await response.json() as {content:Price[]};
 const filtered=payload.content.filter(p=>(!params.country||p.country===params.country)&&(!params.product||p.product===params.product));
 return {content:filtered,totalElements:filtered.length,totalPages:filtered.length?1:0};
}
export async function getBoxBenchmarks():Promise<BoxBenchmark[]>{
 const response=await fetch(`${import.meta.env.BASE_URL}data/benchmarks.json`,{cache:'no-cache'});
 if(!response.ok) throw new Error(`Benchmark data unavailable: ${response.status}`);
 return ((await response.json()) as {content:BoxBenchmark[]}).content;
}
